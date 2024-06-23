package end;

public class Producto {
    private String codigo;
    private String descripcion;
    private String unidadMedida;
    private double saldoInicial;
    private double ingresos;
    private double egresos;
    private double ajustes;
    private double saldoFinal;
    private double costoUM;
    private double precioUM;
    private String status;

    // Getters y Setters

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public double getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(double saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public double getIngresos() {
        return ingresos;
    }

    public void setIngresos(double ingresos) {
        this.ingresos = ingresos;
    }

    public double getEgresos() {
        return egresos;
    }

    public void setEgresos(double egresos) {
        this.egresos = egresos;
    }

    public double getAjustes() {
        return ajustes;
    }

    public void setAjustes(double ajustes) {
        this.ajustes = ajustes;
    }

    public double getSaldoFinal() {
        return saldoFinal;
    }

    public void setSaldoFinal(double saldoFinal) {
        this.saldoFinal = saldoFinal;
    }

    public double getCostoUM() {
        return costoUM;
    }

    public void setCostoUM(double costoUM) {
        this.costoUM = costoUM;
    }

    public double getPrecioUM() {
        return precioUM;
    }

    public void setPrecioUM(double precioUM) {
        this.precioUM = precioUM;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

