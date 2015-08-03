package monitor.arduino.tcc.arduinomonitor;

/**
 * Created by kstanoev on 1/14/2015.
 */
public class EnergyMonitor {
    private String tensao;
    private String corrente;
    private String potenciaAtiva;
    private String potenciaAparente;
    private String fatorDePotencia;
    private String energiaInstantanea;

    public EnergyMonitor (
            String tensao,
            String corrente,
            String potenciaAtiva,
            String potenciaAparente,
            String fatorDePotencia,
            String energiaInstantanea

    ){
        this.tensao = tensao;
        this.corrente = corrente;
        this.potenciaAtiva = potenciaAtiva;
        this.potenciaAparente = potenciaAparente;
        this.fatorDePotencia = fatorDePotencia;
        this.energiaInstantanea = energiaInstantanea;

    }

    public String getTensao() {
        return tensao;
    }

    public void setTensao(String tensao) {
        this.tensao = tensao;
    }

    public String getCorrente() {
        return corrente;
    }

    public void setCorrente(String corrente) {
        this.corrente = corrente;
    }

    public String getPotenciaAtiva() {
        return potenciaAtiva;
    }

    public void setPotenciaAtiva(String potenciaAtiva) {
        this.potenciaAtiva = potenciaAtiva;
    }

    public String getPotenciaAparente() {
        return potenciaAparente;
    }

    public void setPotenciaAparente(String potenciaAparente) {
        this.potenciaAparente = potenciaAparente;
    }

    public String getFatorDePotencia() {
        return fatorDePotencia;
    }

    public void setFatorDePotencia(String fatorDePotencia) {
        this.fatorDePotencia = fatorDePotencia;
    }

    public String getEnergiaInstantanea() {
        return energiaInstantanea;
    }

    public void setEnergiaInstantanea(String energiaInstantanea) {
        this.energiaInstantanea = energiaInstantanea;
    }
}
