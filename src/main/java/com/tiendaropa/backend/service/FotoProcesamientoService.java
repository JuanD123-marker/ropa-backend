package com.tiendaropa.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Convierte una foto de prenda "tal cual" (tomada con el celular) en una foto
 * de catálogo. Si el admin elige quitar el fondo: redimensiona, le quita el
 * fondo con rembg, descarta cualquier resto suelto (ruido, restos del fondo
 * original) quedándose solo con la región de píxeles más grande de la foto
 * -siempre la prenda-, recorta al contorno de esa región y la compone sobre
 * un fondo gris claro uniforme. Si el admin elige no quitar el fondo, solo
 * redimensiona y reempaqueta la foto tal cual llegó.
 */
@Service
public class FotoProcesamientoService {

    private static final Color FONDO = new Color(0xF2, 0xF2, 0xF2);
    private static final int LADO_MAXIMO = 1600; // redimensionar antes de procesar
    private static final int UMBRAL_ALPHA = 128;

    // Toda foto de catálogo (quite o no el fondo) termina en este mismo tamaño
    // cuadrado fijo. Sin esto, una gorra (ancha) y una camiseta (alta) quedan
    // con dimensiones distintas entre sí, y la grilla del catálogo se ve
    // despareja. 1200x1200 es suficiente resolución para verse nítido en
    // pantalla sin pesar demasiado.
    private static final int LADO_CANVAS_FINAL = 1200;

    // Margen que se deja siempre libre alrededor de la prenda dentro del
    // lienzo final (solo aplica al camino con fondo removido): 0.08 = 8% de
    // aire a cada lado, para que la prenda no quede pegada al borde.
    private static final float MARGEN_RELATIVO = 0.08f;

    // Fracción del lado menor de la imagen que se trata siempre como fondo,
    // sin importar el alfa, al detectar la región principal. Evita que
    // restos de ruido pegados al borde de la foto queden "encadenados" a la
    // prenda y arrastren un marco oscuro en el resultado final (la prenda,
    // por cómo se fotografía, siempre tiene margen y nunca toca el borde
    // del encuadre).
    private static final float FRACCION_MARGEN_BORDE = 0.01f;
    private static final int MARGEN_BORDE_MINIMO_PX = 5;

    private final String rembgPath;
    private final String modelo;

    public FotoProcesamientoService(
            @Value("${app.fotos.rembg-path}") String rembgPath,
            @Value("${app.fotos.modelo}") String modelo) {
        this.rembgPath = rembgPath;
        this.modelo = modelo;
    }

    /** Quita el fondo, limpia restos sueltos, recorta y compone sobre el gris estándar. Devuelve JPEG. */
    public byte[] procesarConFondoLimpio(byte[] original) throws IOException, InterruptedException {
        BufferedImage imagen = leerYRedimensionar(original);

        Path entrada = Files.createTempFile("foto-prenda-", ".png");
        Path salida = Files.createTempFile("foto-prenda-sin-fondo-", ".png");
        try {
            ImageIO.write(imagen, "png", entrada.toFile());
            ejecutarRembg(entrada, salida);

            BufferedImage sinFondo = ImageIO.read(salida.toFile());
            BufferedImage limpia = conservarSoloComponenteMasGrande(sinFondo);
            BufferedImage recortada = recortarAlContenido(limpia);
            BufferedImage compuesta = componerSobreFondo(recortada);

            return aJpeg(compuesta);
        } finally {
            Files.deleteIfExists(entrada);
            Files.deleteIfExists(salida);
        }
    }

    /**
     * El admin eligió NO quitar el fondo: se deja la foto tal cual llegó (fondo
     * real de la tienda incluido), pero igual se recorta al centro en cuadrado
     * y se escala al mismo tamaño final que el otro camino, para que en la
     * grilla del catálogo todas las fotos se vean parejas.
     */
    public byte[] procesarSinQuitarFondo(byte[] original) throws IOException {
        BufferedImage imagen = leerYRedimensionar(original);
        BufferedImage cuadrada = recortarCuadradoCentrado(imagen);
        BufferedImage aTamanoFinal = escalarCuadrado(cuadrada, LADO_CANVAS_FINAL);

        // Si la foto original tuviera transparencia (poco común), se aplana sobre blanco.
        BufferedImage plana = new BufferedImage(aTamanoFinal.getWidth(), aTamanoFinal.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = plana.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, plana.getWidth(), plana.getHeight());
        g.drawImage(aTamanoFinal, 0, 0, null);
        g.dispose();

        return aJpeg(plana);
    }

    private BufferedImage leerYRedimensionar(byte[] original) throws IOException {
        BufferedImage imagen = ImageIO.read(new ByteArrayInputStream(original));
        if (imagen == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo enviado no es una imagen válida");
        }

        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();
        int ladoMayor = Math.max(ancho, alto);
        if (ladoMayor <= LADO_MAXIMO) {
            return imagen;
        }

        double escala = (double) LADO_MAXIMO / ladoMayor;
        int nuevoAncho = (int) Math.round(ancho * escala);
        int nuevoAlto = (int) Math.round(alto * escala);

        BufferedImage redimensionada = new BufferedImage(nuevoAncho, nuevoAlto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = redimensionada.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(imagen, 0, 0, nuevoAncho, nuevoAlto, null);
        g.dispose();
        return redimensionada;
    }

    private void ejecutarRembg(Path entrada, Path salida) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                rembgPath, "i", "-m", modelo,
                entrada.toAbsolutePath().toString(),
                salida.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process proceso = pb.start();

        // Hay que drenar la salida del proceso: si su buffer se llena y nadie lo lee, rembg se queda colgado.
        try (InputStream in = proceso.getInputStream()) {
            in.readAllBytes();
        }

        boolean termino = proceso.waitFor(60, TimeUnit.SECONDS);
        if (!termino) {
            proceso.destroyForcibly();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "rembg tardó demasiado procesando la foto");
        }
        if (proceso.exitValue() != 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "rembg no pudo procesar la foto (código " + proceso.exitValue() + ")");
        }
    }

    /**
     * Encuentra las regiones conectadas de píxeles no transparentes y descarta
     * todas menos la más grande (se asume que la prenda es siempre la región
     * más grande de la foto). Esto elimina de un solo paso tanto el ruido
     * suelto como restos más grandes (ej. el letrero de neón del fondo).
     *
     * Antes de etiquetar, se fuerza una franja angosta del borde a "fondo"
     * (ver {@link #FRACCION_MARGEN_BORDE}) para que restos de ruido pegados
     * al borde de la foto no queden conectados con la prenda formando una
     * sola región gigante.
     */
    private BufferedImage conservarSoloComponenteMasGrande(BufferedImage imagen) {
        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();
        int total = ancho * alto;
        int margenBorde = Math.max(MARGEN_BORDE_MINIMO_PX, Math.round(Math.min(ancho, alto) * FRACCION_MARGEN_BORDE));

        boolean[] esPrimerPlano = new boolean[total];
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                boolean enBorde = x < margenBorde || x >= ancho - margenBorde
                        || y < margenBorde || y >= alto - margenBorde;
                if (enBorde) {
                    esPrimerPlano[y * ancho + x] = false;
                    continue;
                }
                int alfa = (imagen.getRGB(x, y) >>> 24) & 0xFF;
                esPrimerPlano[y * ancho + x] = alfa >= UMBRAL_ALPHA;
            }
        }

        int[] etiquetas = new int[total];
        int siguienteEtiqueta = 1;
        Map<Integer, Integer> tamanoPorEtiqueta = new HashMap<>();
        Deque<Integer> pila = new ArrayDeque<>();

        for (int i = 0; i < total; i++) {
            if (!esPrimerPlano[i] || etiquetas[i] != 0) continue;

            int etiqueta = siguienteEtiqueta++;
            int tamano = 0;
            pila.push(i);
            etiquetas[i] = etiqueta;

            while (!pila.isEmpty()) {
                int actual = pila.pop();
                tamano++;
                int cx = actual % ancho;
                int cy = actual / ancho;

                int[][] vecinos = {{cx - 1, cy}, {cx + 1, cy}, {cx, cy - 1}, {cx, cy + 1}};
                for (int[] v : vecinos) {
                    int vx = v[0], vy = v[1];
                    if (vx < 0 || vx >= ancho || vy < 0 || vy >= alto) continue;
                    int idx = vy * ancho + vx;
                    if (esPrimerPlano[idx] && etiquetas[idx] == 0) {
                        etiquetas[idx] = etiqueta;
                        pila.push(idx);
                    }
                }
            }
            tamanoPorEtiqueta.put(etiqueta, tamano);
        }

        if (tamanoPorEtiqueta.isEmpty()) {
            return imagen; // rembg no dejó nada en primer plano; se deja igual
        }

        int etiquetaGanadora = tamanoPorEtiqueta.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow()
                .getKey();

        BufferedImage limpia = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int idx = y * ancho + x;
                if (etiquetas[idx] == etiquetaGanadora) {
                    limpia.setRGB(x, y, imagen.getRGB(x, y));
                }
                // todo lo demás queda transparente (valor por defecto)
            }
        }
        return limpia;
    }

    /** Recorta la imagen al cuadro delimitador del contenido no transparente, con un margen pequeño. */
    private BufferedImage recortarAlContenido(BufferedImage imagen) {
        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();
        int minX = ancho, minY = alto, maxX = -1, maxY = -1;

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int alfa = (imagen.getRGB(x, y) >>> 24) & 0xFF;
                if (alfa >= UMBRAL_ALPHA) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return imagen; // no se detectó contenido; se deja igual
        }

        int margen = (int) (Math.max(maxX - minX, maxY - minY) * 0.05);
        minX = Math.max(0, minX - margen);
        minY = Math.max(0, minY - margen);
        maxX = Math.min(ancho - 1, maxX + margen);
        maxY = Math.min(alto - 1, maxY + margen);

        return imagen.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    /**
     * Compone la prenda (recortada, con transparencia) centrada sobre un
     * lienzo cuadrado fijo de fondo gris claro. Sin esto, una gorra (ancha)
     * y una camiseta (alta) saldrían con dimensiones y proporciones
     * distintas entre sí; con esto, toda foto del catálogo mide siempre
     * {@link #LADO_CANVAS_FINAL}×{@link #LADO_CANVAS_FINAL}, con la prenda
     * centrada y un margen parejo alrededor (nunca se agranda una prenda
     * más allá de su resolución real, para no verse borrosa).
     */
    private BufferedImage componerSobreFondo(BufferedImage imagenRecortada) {
        BufferedImage lienzo = new BufferedImage(LADO_CANVAS_FINAL, LADO_CANVAS_FINAL, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = lienzo.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(FONDO);
        g.fillRect(0, 0, LADO_CANVAS_FINAL, LADO_CANVAS_FINAL);

        int areaDisponible = Math.round(LADO_CANVAS_FINAL * (1 - 2 * MARGEN_RELATIVO));
        int anchoOriginal = imagenRecortada.getWidth();
        int altoOriginal = imagenRecortada.getHeight();
        double escala = Math.min(
                (double) areaDisponible / anchoOriginal,
                (double) areaDisponible / altoOriginal
        );
        escala = Math.min(escala, 1.0); // nunca agrandar una foto más allá de su tamaño real

        int anchoFinal = Math.max(1, (int) Math.round(anchoOriginal * escala));
        int altoFinal = Math.max(1, (int) Math.round(altoOriginal * escala));
        int x = (LADO_CANVAS_FINAL - anchoFinal) / 2;
        int y = (LADO_CANVAS_FINAL - altoFinal) / 2;

        g.drawImage(imagenRecortada, x, y, anchoFinal, altoFinal, null);
        g.dispose();
        return lienzo;
    }

    /** Recorta al centro el cuadrado más grande posible (estilo "cover"), sin estirar. */
    private BufferedImage recortarCuadradoCentrado(BufferedImage imagen) {
        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();
        int lado = Math.min(ancho, alto);
        int x = (ancho - lado) / 2;
        int y = (alto - lado) / 2;
        return imagen.getSubimage(x, y, lado, lado);
    }

    /** Escala una imagen cuadrada al lado destino (puede agrandar o achicar). */
    private BufferedImage escalarCuadrado(BufferedImage imagenCuadrada, int ladoDestino) {
        if (imagenCuadrada.getWidth() == ladoDestino) {
            return imagenCuadrada;
        }
        BufferedImage escalada = new BufferedImage(ladoDestino, ladoDestino, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = escalada.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(imagenCuadrada, 0, 0, ladoDestino, ladoDestino, null);
        g.dispose();
        return escalada;
    }

    private byte[] aJpeg(BufferedImage imagen) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen, "jpg", salida);
        return salida.toByteArray();
    }
}
