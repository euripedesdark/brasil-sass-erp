package br.com.brasil_saas.enterprise.service;

import br.com.brasil_saas.shared.exception.BusinessException;

import java.math.BigDecimal;
import java.util.*;

/** Agrupa entregas em cargas respeitando capacidade de peso e volume (first-fit decreasing por destino). */
public final class PlanejadorCarga {
    private PlanejadorCarga() {}

    public record Entrega(String referenciaTipo, Long referenciaId, String destino, BigDecimal peso, BigDecimal volume) {}

    public static final class Carga {
        private final String destino;
        private final List<Entrega> entregas = new ArrayList<>();
        private BigDecimal peso = BigDecimal.ZERO, volume = BigDecimal.ZERO;
        Carga(String destino) { this.destino = destino; }
        public String destino() { return destino; }
        public List<Entrega> entregas() { return entregas; }
        public BigDecimal peso() { return peso; }
        public BigDecimal volume() { return volume; }
        boolean cabe(Entrega e, BigDecimal capPeso, BigDecimal capVolume) {
            return peso.add(e.peso()).compareTo(capPeso) <= 0 && volume.add(e.volume()).compareTo(capVolume) <= 0;
        }
        void add(Entrega e) { entregas.add(e); peso = peso.add(e.peso()); volume = volume.add(e.volume()); }
    }

    public static List<Carga> planejar(List<Entrega> entregas, BigDecimal capPeso, BigDecimal capVolume) {
        if (capPeso == null || capPeso.signum() <= 0 || capVolume == null || capVolume.signum() <= 0)
            throw new BusinessException("Capacidade de peso e volume deve ser maior que zero");
        Map<String, List<Entrega>> porDestino = new TreeMap<>();
        for (Entrega e : entregas) {
            if (e.peso() == null || e.volume() == null || e.peso().signum() < 0 || e.volume().signum() < 0)
                throw new BusinessException("Entrega " + e.referenciaId() + " sem peso/volume válido");
            if (e.peso().compareTo(capPeso) > 0 || e.volume().compareTo(capVolume) > 0)
                throw new BusinessException("Entrega " + e.referenciaId() + " excede a capacidade de um veículo");
            porDestino.computeIfAbsent(e.destino() == null ? "" : e.destino().trim().toUpperCase(), k -> new ArrayList<>()).add(e);
        }
        List<Carga> cargas = new ArrayList<>();
        for (var entry : porDestino.entrySet()) {
            List<Entrega> lista = new ArrayList<>(entry.getValue());
            lista.sort(Comparator.comparing(Entrega::peso).reversed());
            List<Carga> doDestino = new ArrayList<>();
            for (Entrega e : lista) {
                Carga alvo = doDestino.stream().filter(c -> c.cabe(e, capPeso, capVolume)).findFirst().orElse(null);
                if (alvo == null) { alvo = new Carga(entry.getKey()); doDestino.add(alvo); }
                alvo.add(e);
            }
            cargas.addAll(doDestino);
        }
        return cargas;
    }
}
