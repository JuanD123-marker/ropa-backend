package com.tiendaropa.backend.service;

import com.tiendaropa.backend.dto.PrendaEditRequest;
import com.tiendaropa.backend.dto.PrendaRequest;
import com.tiendaropa.backend.dto.PrendaResponse;
import com.tiendaropa.backend.dto.VarianteEditRequest;
import com.tiendaropa.backend.dto.VarianteRequest;
import com.tiendaropa.backend.dto.VarianteResponse;
import com.tiendaropa.backend.model.FotoPrenda;
import com.tiendaropa.backend.model.ItemPedido;
import com.tiendaropa.backend.model.Prenda;
import com.tiendaropa.backend.model.Tienda;
import com.tiendaropa.backend.model.VarianteTalla;
import com.tiendaropa.backend.model.enums.EstadoPrenda;
import com.tiendaropa.backend.repository.FotoPrendaRepository;
import com.tiendaropa.backend.repository.ItemPedidoRepository;
import com.tiendaropa.backend.repository.PrendaRepository;
import com.tiendaropa.backend.repository.TiendaRepository;
import com.tiendaropa.backend.repository.VarianteTallaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Publicar una prenda con sus fotos y variantes. La limpieza de fondo de
 * las fotos pasa ANTES de llegar aquí (otro servicio/paso del flujo); este
 * método solo persiste el resultado.
 *
 * Los controladores nunca devuelven las entidades JPA directamente (tienen
 * relaciones lazy que no se deben serializar tal cual) — todo pasa por
 * {@link #aRespuesta(Prenda)}, que arma el DTO plano consultando fotos y
 * variantes por separado.
 */
@Service
@RequiredArgsConstructor
public class PrendaService {

    private final PrendaRepository prendaRepository;
    private final FotoPrendaRepository fotoPrendaRepository;
    private final VarianteTallaRepository varianteTallaRepository;
    private final TiendaRepository tiendaRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    @Transactional
    public PrendaResponse crear(Long tiendaId, PrendaRequest request) {
        Tienda tienda = tiendaRepository.findById(tiendaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tienda no encontrada"));

        Prenda prenda = Prenda.builder()
                .tienda(tienda)
                .nombre(request.getNombre())
                .categoria(request.getCategoria())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .precioOferta(request.getPrecioOferta())
                .costo(request.getCosto())
                .estado(EstadoPrenda.PUBLICADA)
                .build();
        prenda = prendaRepository.save(prenda);

        List<String> fotos = request.getFotosUrl();
        if (fotos != null) {
            for (int i = 0; i < fotos.size(); i++) {
                fotoPrendaRepository.save(FotoPrenda.builder()
                        .prenda(prenda)
                        .url(fotos.get(i))
                        .orden(i)
                        .principal(i == 0)
                        .build());
            }
        }

        for (VarianteRequest v : request.getVariantes()) {
            varianteTallaRepository.save(VarianteTalla.builder()
                    .prenda(prenda)
                    .talla(v.getTalla())
                    .color(v.getColor())
                    .stock(v.getStock())
                    .build());
        }

        return aRespuesta(prenda);
    }

    public List<PrendaResponse> listarPublicadas(Long tiendaId) {
        return prendaRepository.findByTiendaIdAndEstado(tiendaId, EstadoPrenda.PUBLICADA)
                .stream()
                .map(this::aRespuesta)
                .collect(Collectors.toList());
    }

    public List<PrendaResponse> listarDeLaTienda(Long tiendaId) {
        return prendaRepository.findByTiendaId(tiendaId)
                .stream()
                .map(this::aRespuesta)
                .collect(Collectors.toList());
    }

    /**
     * Edición parcial: solo se cambian los campos que vengan distintos de
     * null. fotosUrl y variantes, cuando vienen, reemplazan el set completo
     * (ver {@link PrendaEditRequest}).
     */
    @Transactional
    public PrendaResponse editar(Long tiendaId, Long prendaId, PrendaEditRequest request) {
        Prenda prenda = obtenerDeLaTienda(tiendaId, prendaId);

        if (request.getNombre() != null) prenda.setNombre(request.getNombre());
        if (request.getCategoria() != null) prenda.setCategoria(request.getCategoria());
        if (request.getDescripcion() != null) prenda.setDescripcion(request.getDescripcion());
        if (request.getPrecio() != null) prenda.setPrecio(request.getPrecio());
        if (request.getPrecioOferta() != null) prenda.setPrecioOferta(request.getPrecioOferta());
        if (request.getCosto() != null) prenda.setCosto(request.getCosto());

        prenda = prendaRepository.save(prenda);

        if (request.getFotosUrl() != null) {
            reemplazarFotos(prenda, request.getFotosUrl());
        }

        if (request.getVariantes() != null) {
            reemplazarVariantes(prenda, request.getVariantes());
        }

        return aRespuesta(prenda);
    }

    /** Pausar/publicar/marcar agotada una prenda (cambia solo el estado). */
    @Transactional
    public PrendaResponse cambiarEstado(Long tiendaId, Long prendaId, EstadoPrenda nuevoEstado) {
        Prenda prenda = obtenerDeLaTienda(tiendaId, prendaId);
        prenda.setEstado(nuevoEstado);
        prenda = prendaRepository.save(prenda);
        return aRespuesta(prenda);
    }

    /** Ajuste manual de stock de una variante puntual (ej. conteo físico distinto al del sistema). */
    @Transactional
    public VarianteResponse ajustarStock(Long tiendaId, Long varianteId, int nuevoStock) {
        VarianteTalla variante = varianteTallaRepository.findById(varianteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variante no encontrada"));

        if (!variante.getPrenda().getTienda().getId().equals(tiendaId)) {
            // Mismo 404 que "no existe": no revelamos que la variante es de otra tienda.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Variante no encontrada");
        }

        variante.setStock(nuevoStock);
        variante = varianteTallaRepository.save(variante);

        return VarianteResponse.builder()
                .id(variante.getId())
                .talla(variante.getTalla())
                .color(variante.getColor())
                .stock(variante.getStock())
                .build();
    }

    /**
     * Elimina una prenda por completo (fotos, variantes y la prenda misma) —
     * sin importar si ya tiene ventas registradas. El inventario rota (hoy
     * se vende una prenda, mañana ya no se va a volver a tener igual), así
     * que bloquear el borrado no es viable a la larga; "pausarla" sigue
     * existiendo para el caso de solo quitarla del catálogo temporalmente.
     *
     * Antes de borrar cada variante, {@link #desvincularPedidosDe} convierte
     * sus líneas de venta ya registradas en "ventas sueltas" (mismo mecanismo
     * que ya existía para una venta física sin catálogo): el nombre de la
     * prenda/talla/color queda guardado como texto en esa línea del pedido,
     * así que Finanzas sigue mostrando exactamente qué se vendió aunque la
     * prenda ya no exista — simplemente deja de estar "en vivo" enlazada al
     * inventario.
     */
    @Transactional
    public void eliminar(Long tiendaId, Long prendaId) {
        Prenda prenda = obtenerDeLaTienda(tiendaId, prendaId);

        fotoPrendaRepository.deleteAll(fotoPrendaRepository.findByPrendaIdOrderByOrdenAsc(prendaId));

        List<VarianteTalla> variantes = varianteTallaRepository.findByPrendaId(prendaId);
        desvincularPedidosDe(variantes);
        varianteTallaRepository.deleteAll(variantes);

        prendaRepository.delete(prenda);
    }

    /**
     * Para cada variante que se va a borrar, busca sus líneas de venta ya
     * registradas (si las tiene) y las "desvincula": les guarda el nombre de
     * la prenda + talla/color en {@code nombreLibre} y les quita la
     * referencia a la variante. Así la variante queda libre para borrarse
     * sin romper integridad referencial, y la venta pasada no desaparece ni
     * queda huérfana — pasa a verse como una venta suelta de ese momento.
     */
    private void desvincularPedidosDe(List<VarianteTalla> variantes) {
        if (variantes.isEmpty()) {
            return;
        }

        List<Long> ids = variantes.stream().map(VarianteTalla::getId).collect(Collectors.toList());
        List<ItemPedido> items = itemPedidoRepository.findByVarianteIdIn(ids);
        if (items.isEmpty()) {
            return;
        }

        Map<Long, VarianteTalla> variantesPorId = variantes.stream()
                .collect(Collectors.toMap(VarianteTalla::getId, v -> v));

        for (ItemPedido item : items) {
            VarianteTalla variante = variantesPorId.get(item.getVariante().getId());
            item.setNombreLibre(nombreHistorico(variante));
            item.setVariante(null);
        }
        itemPedidoRepository.saveAll(items);
        itemPedidoRepository.flush();
    }

    /** Texto que queda guardado en una venta pasada cuando su variante se borra — ej. "Gorra blanca (Única, blanco)". */
    private String nombreHistorico(VarianteTalla variante) {
        String detalle = variante.getTalla();
        if (variante.getColor() != null && !variante.getColor().isBlank()) {
            detalle = detalle + ", " + variante.getColor();
        }
        return variante.getPrenda().getNombre() + " (" + detalle + ")";
    }

    /** Busca la prenda y confirma que pertenece a la tienda del administrador autenticado. */
    private Prenda obtenerDeLaTienda(Long tiendaId, Long prendaId) {
        Prenda prenda = prendaRepository.findById(prendaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prenda no encontrada"));

        if (!prenda.getTienda().getId().equals(tiendaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Prenda no encontrada");
        }

        return prenda;
    }

    /** Reemplaza el set completo de fotos de la prenda por la lista recibida, en ese orden. */
    private void reemplazarFotos(Prenda prenda, List<String> nuevasUrls) {
        if (nuevasUrls.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La prenda necesita al menos una foto");
        }

        fotoPrendaRepository.deleteAll(fotoPrendaRepository.findByPrendaIdOrderByOrdenAsc(prenda.getId()));

        for (int i = 0; i < nuevasUrls.size(); i++) {
            fotoPrendaRepository.save(FotoPrenda.builder()
                    .prenda(prenda)
                    .url(nuevasUrls.get(i))
                    .orden(i)
                    .principal(i == 0)
                    .build());
        }
    }

    /**
     * Reemplaza el set completo de variantes de talla: actualiza las que
     * traen "id", crea las que no lo traen, y elimina las existentes que no
     * vinieron en la lista. Si una variante a eliminar ya tiene pedidos
     * registrados, esas ventas se desvinculan primero (ver
     * {@link #desvincularPedidosDe}) en vez de bloquear el cambio — el
     * inventario rota y no debería depender de que una talla nunca se haya
     * vendido para poder quitarla. Si una variante a crear choca con otra de
     * la misma talla+color, sí se avisa con un mensaje claro.
     */
    private void reemplazarVariantes(Prenda prenda, List<VarianteEditRequest> nuevasVariantes) {
        if (nuevasVariantes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La prenda necesita al menos una talla");
        }

        List<VarianteTalla> existentes = varianteTallaRepository.findByPrendaId(prenda.getId());

        Set<Long> idsEnviados = nuevasVariantes.stream()
                .map(VarianteEditRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<VarianteTalla> aEliminar = existentes.stream()
                .filter(existente -> !idsEnviados.contains(existente.getId()))
                .collect(Collectors.toList());

        desvincularPedidosDe(aEliminar);
        varianteTallaRepository.deleteAll(aEliminar);
        varianteTallaRepository.flush();

        for (VarianteEditRequest v : nuevasVariantes) {
            try {
                if (v.getId() != null) {
                    VarianteTalla existente = existentes.stream()
                            .filter(e -> e.getId().equals(v.getId()))
                            .findFirst()
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variante no encontrada"));
                    existente.setTalla(v.getTalla());
                    existente.setColor(v.getColor());
                    existente.setStock(v.getStock());
                    varianteTallaRepository.save(existente);
                } else {
                    varianteTallaRepository.save(VarianteTalla.builder()
                            .prenda(prenda)
                            .talla(v.getTalla())
                            .color(v.getColor())
                            .stock(v.getStock())
                            .build());
                }
                varianteTallaRepository.flush();
            } catch (DataIntegrityViolationException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ya existe una variante con la talla \"" + v.getTalla() + "\"" +
                                (v.getColor() != null && !v.getColor().isBlank() ? " y color \"" + v.getColor() + "\"" : "") +
                                ".");
            }
        }
    }

    /** Arma el DTO plano de una prenda, trayendo sus fotos y variantes por separado (relaciones lazy). */
    private PrendaResponse aRespuesta(Prenda prenda) {
        List<String> fotosUrl = fotoPrendaRepository.findByPrendaIdOrderByOrdenAsc(prenda.getId())
                .stream()
                .map(FotoPrenda::getUrl)
                .collect(Collectors.toList());

        List<VarianteResponse> variantes = varianteTallaRepository.findByPrendaId(prenda.getId())
                .stream()
                .map(v -> VarianteResponse.builder()
                        .id(v.getId())
                        .talla(v.getTalla())
                        .color(v.getColor())
                        .stock(v.getStock())
                        .build())
                .collect(Collectors.toList());

        return PrendaResponse.builder()
                .id(prenda.getId())
                .nombre(prenda.getNombre())
                .categoria(prenda.getCategoria())
                .descripcion(prenda.getDescripcion())
                .precio(prenda.getPrecio())
                .precioOferta(prenda.getPrecioOferta())
                .costo(prenda.getCosto())
                .estado(prenda.getEstado())
                .fechaCreacion(prenda.getFechaCreacion())
                .fotosUrl(fotosUrl)
                .variantes(variantes)
                .build();
    }
}
