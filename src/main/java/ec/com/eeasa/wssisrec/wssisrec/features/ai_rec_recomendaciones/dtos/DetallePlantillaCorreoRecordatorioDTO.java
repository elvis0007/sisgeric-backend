package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos;


/*
 * Autor: Mateo Díaz
 */
public class DetallePlantillaCorreoRecordatorioDTO {
    //VAR
    private String nombres;
    private String apellidos;
    private String tituloInforme;
    private int conteoRecomendacionesPendientes;
    //CONSTRUCTOR
    public DetallePlantillaCorreoRecordatorioDTO (String nombres, String apellidos, String tituloInforme, int conteoRecomendacionesPendientes){
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.tituloInforme = tituloInforme;
        this.conteoRecomendacionesPendientes = conteoRecomendacionesPendientes;
    }
    //GETTERS METHODS
    public String getNombres(){ return this.nombres; }
    public String getApellidos(){ return this.apellidos; }
    public String getTituloInforme(){ return this.tituloInforme; }
    public int getConteoRecomendacionesPendientes(){ return this.conteoRecomendacionesPendientes; }
}
