package kz.assem.biomedical;

public record Measurements(double quality, long alarms, double latencyMs) {
    public Measurements {
        if (!Double.isFinite(quality) || quality < 0 || quality > 100 || alarms < 0
                || !Double.isFinite(latencyMs) || latencyMs < 0) {
            throw new IllegalArgumentException("Invalid measurements.");
        }
    }
}

