/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package paq_manauto;

import framework.aplicacion.TablaGenerica;
import framework.componentes.AutoCompletar;
import framework.componentes.Boton;
import framework.componentes.Calendario;
import framework.componentes.Dialogo;
import framework.componentes.Etiqueta;
import framework.componentes.Grid;
import framework.componentes.Grupo;
import framework.componentes.Panel;
import framework.componentes.PanelTabla;
import framework.componentes.SeleccionTabla;
import framework.componentes.Tabla;
import framework.componentes.Texto;
import javax.ejb.EJB;
import org.primefaces.event.SelectEvent;
import paq_manauto.ejb.SQLManauto;
import paq_manauto.ejb.manauto;
import paq_sistema.aplicacion.Pantalla;
import persistencia.Conexion;

/**
 *
 * @author p-sistemas
 */
public class AbastecimientoMaquinaria extends Pantalla {

    private Conexion conPostgres = new Conexion();
    private Tabla tabConsulta = new Tabla();
    private Tabla tabTabla = new Tabla();
    private Tabla setDepencias = new Tabla();
    private SeleccionTabla setTabla = new SeleccionTabla();
    private Panel panOpcion = new Panel();
    private AutoCompletar autCompleta = new AutoCompletar();
    private Calendario calFechaInicio = new Calendario();
    private Calendario calFechaFin = new Calendario();
    private Dialogo dialogoa = new Dialogo();
    private Grid grid = new Grid();
    private Grid grida = new Grid();
    private Texto taccesorio = new Texto();
    @EJB
    private manauto aCombustible = (manauto) utilitario.instanciarEJB(manauto.class);
    private SQLManauto bCombustible = (SQLManauto) utilitario.instanciarEJB(SQLManauto.class);

    public AbastecimientoMaquinaria() {
        tabConsulta.setId("tabConsulta");
        tabConsulta.setSql("SELECT u.IDE_USUA,u.NOM_USUA,u.NICK_USUA,u.IDE_PERF,p.NOM_PERF,p.PERM_UTIL_PERF\n"
                + "FROM SIS_USUARIO u,SIS_PERFIL p where u.IDE_PERF = p.IDE_PERF and IDE_USUA=" + utilitario.getVariable("IDE_USUA"));
        tabConsulta.setCampoPrimaria("IDE_USUA");
        tabConsulta.setLectura(true);
        tabConsulta.dibujar();

        conPostgres.setUnidad_persistencia(utilitario.getPropiedad("poolPostgres"));
        conPostgres.NOMBRE_MARCA_BASE = "postgres";

        Boton botBuscar = new Boton();
        botBuscar.setValue("Buscar Registro");
        botBuscar.setIcon("ui-icon-search");
        botBuscar.setMetodo("abrirCuadro");
        bar_botones.agregarBoton(botBuscar);

        autCompleta.setId("autCompleta");
        autCompleta.setConexion(conPostgres);
        autCompleta.setAutoCompletar("SELECT a.abastecimiento_id,a.abastecimiento_fecha,a.abastecimiento_numero_vale, \n"
                + "(case when a.mve_secuencial is not null then (case when v.placa is not null then v.placa when v.placa is null then v.codigo_activo end )    \n"
                + "when a.mve_secuencial is null then d.dependencia_descripcion end)    \n"
                + "FROM mvabactecimiento_combustible AS a \n"
                + "left JOIN mv_vehiculo v ON a.mve_secuencial = v.mve_secuencial \n"
                + "left join mvtipo_dependencias d on a.abastecimiento_cod_dependencia = d.dependencia_codigo \n"
                + "WHERE a.abastecimiento_tipo_ingreso = 'H'\n"
                + "ORDER BY a.abastecimiento_fecha ASC, a.abastecimiento_numero_vale ASC");
        autCompleta.setMetodoChange("filtrarSolicitud");
        autCompleta.setSize(70);
        bar_botones.agregarComponente(new Etiqueta("Registros Encontrado"));
        bar_botones.agregarComponente(autCompleta);

        Boton botDependencia = new Boton();
        botDependencia.setValue("Dependencias");
        botDependencia.setIcon("ui-icon-plus");
        botDependencia.setMetodo("ingDependencia");
        bar_botones.agregarBoton(botDependencia);

        /*CONFIGURACIÓN DE COMBOS*/
        Grid griBusca = new Grid();
        griBusca.setColumns(2);

        griBusca.getChildren().add(new Etiqueta("FECHA INICIO:"));
        griBusca.getChildren().add(calFechaInicio);
        griBusca.getChildren().add(new Etiqueta("FECHA FINAL:"));
        griBusca.getChildren().add(calFechaFin);

        Boton botAcceso = new Boton();
        botAcceso.setValue("Buscar");
        botAcceso.setIcon("ui-icon-search");
        botAcceso.setMetodo("aceptarRegistro");
        bar_botones.agregarBoton(botAcceso);
        griBusca.getChildren().add(botAcceso);

        setTabla.setId("setTabla");
        setTabla.getTab_seleccion().setConexion(conPostgres);
        setTabla.setSeleccionTabla("SELECT a.abastecimiento_id,a.abastecimiento_fecha,a.abastecimiento_numero_vale,\n"
                + "(case when a.mve_secuencial is not null then (case when v.placa is not null then v.placa when v.placa is null then v.codigo_activo end ) \n"
                + "when a.mve_secuencial is null then d.dependencia_descripcion end )  as descripcion  \n"
                + "FROM mvabactecimiento_combustible AS a  \n"
                + "left JOIN mv_vehiculo v ON a.mve_secuencial = v.mve_secuencial   \n"
                + "left join mvtipo_dependencias d on a.abastecimiento_cod_dependencia = d.dependencia_codigo  \n"
                + "WHERE a.abastecimiento_tipo_ingreso = 'H'\n"
                + "ORDER BY a.abastecimiento_fecha ASC, a.abastecimiento_numero_vale ASC", "abastecimiento_id");
        setTabla.getTab_seleccion().setEmptyMessage("No Encuentra Datos");
        setTabla.getTab_seleccion().setRows(10);
        setTabla.setRadio();
        setTabla.getGri_cuerpo().setHeader(griBusca);
        setTabla.getBot_aceptar().setMetodo("buscarRegistro");
        setTabla.setHeader("REPORTES DE DESCUENTOS - SELECCIONE PARAMETROS");
        agregarComponente(setTabla);

        panOpcion.setId("panOpcion");
        panOpcion.setTransient(true);
        panOpcion.setHeader("ABASTECIMIENTO DE COMBUSTIBLE");
        agregarComponente(panOpcion);

        //Para accesorios
        Grid griDependencia = new Grid();
        griDependencia.setColumns(2);
        griDependencia.getChildren().add(new Etiqueta("Dependencia"));
        griDependencia.getChildren().add(taccesorio);
        Boton botDepenIng = new Boton();
        botDepenIng.setValue("Guardar");
        botDepenIng.setIcon("ui-icon-disk");
        botDepenIng.setMetodo("insAccesorio");
        bar_botones.agregarBoton(botDepenIng);
        Boton botDepenBor = new Boton();
        botDepenBor.setValue("Eliminar");
        botDepenBor.setIcon("ui-icon-closethick");
        botDepenBor.setMetodo("endAccesorio");
        bar_botones.agregarBoton(botDepenBor);
        griDependencia.getChildren().add(botDepenIng);
        griDependencia.getChildren().add(botDepenBor);
        dialogoa.setId("dialogoa");
        dialogoa.setTitle("DEPENDENCIA SOLICITA COMBUSTIBLE"); //titulo
        dialogoa.setWidth("38%"); //siempre en porcentajes  ancho
        dialogoa.setHeight("40%");//siempre porcentaje   alto
        dialogoa.setResizable(false); //para que no se pueda cambiar el tamaño
        dialogoa.getGri_cuerpo().setHeader(griDependencia);
        grid.setColumns(4);
        agregarComponente(dialogoa);

        dibujarPantalla();
    }

    public void dibujarPantalla() {
        limpiarPanel();
        tabTabla.setId("tabTabla");
        tabTabla.setConexion(conPostgres);
        tabTabla.setTabla("mvabactecimiento_combustible", "abastecimiento_id", 1);
        if (autCompleta.getValue() == null) {
            tabTabla.setCondicion("abastecimiento_id=-1");
        } else {
            tabTabla.setCondicion("abastecimiento_id=" + autCompleta.getValor());
        }
        tabTabla.getColumna("tipo_combustible_id").setCombo("SELECT tipo_combustible_id,(tipo_combustible_descripcion||'/'||tipo_valor_galon) as valor FROM mvtipo_combustible order by tipo_combustible_descripcion");
        tabTabla.getColumna("mve_secuencial").setCombo("SELECT v.mve_secuencial,   \n"
                + "(case when m.mvmarca_descripcion is null then v.motor when m.mvmarca_descripcion is not null then ((case when v.placa is NULL then v.codigo_activo when v.placa is not null then v.placa end )\n"
                + "||'/'||m.mvmarca_descripcion ||'/'||o.mvmodelo_descripcion)end)as descripcion  \n"
                + "FROM mv_vehiculo v  \n"
                + "LEFT JOIN mvmarca_vehiculo m ON v.marca_id = m.mvmarca_id  \n"
                + "LEFT JOIN mvmodelo_vehiculo o ON v.modelo_id = o.mvmodelo_id  \n"
                + "WHERE v.tipo_ingreso = 'M' or v.placa='0000000'");
        tabTabla.getColumna("abastecimiento_cod_conductor").setCombo("SELECT cod_empleado,nombres FROM srh_empleado where estado = 1 order by nombres");
        tabTabla.getColumna("abastecimiento_cod_dependencia").setCombo("SELECT dependencia_codigo,dependencia_descripcion from mvtipo_dependencias order by dependencia_descripcion");
        tabTabla.getColumna("abastecimiento_cod_dependencia").setFiltroContenido();
        tabTabla.getColumna("mve_secuencial").setFiltroContenido();
        tabTabla.getColumna("mve_secuencial").setMetodoChange("busPlaca");
        tabTabla.getColumna("abastecimiento_galones").setMetodoChange("carga");
        tabTabla.getColumna("abastecimiento_valorhora").setMetodoChange("horaActu");
        tabTabla.getColumna("abastecimiento_tipo_medicion").setValorDefecto("2");
        tabTabla.getColumna("abastecimiento_valorhora").setMascara("99:99");
        tabTabla.getColumna("abastecimiento_logining").setValorDefecto(tabConsulta.getValor("NICK_USUA"));
        tabTabla.getColumna("abastecimiento_fechaing").setValorDefecto(utilitario.getFechaActual());
        tabTabla.getColumna("abastecimiento_horaing").setValorDefecto(utilitario.getHoraActual());
        tabTabla.getColumna("tipo_combustible_id").setLectura(true);
        tabTabla.getColumna("abastecimiento_numero").setLectura(true);
        tabTabla.getColumna("abastecimiento_total").setLectura(true);
        tabTabla.getColumna("abastecimiento_cod_dependencia").setLectura(true);
        tabTabla.getColumna("abastecimiento_id").setVisible(false);
        tabTabla.getColumna("abastecimiento_fechaing").setVisible(false);
        tabTabla.getColumna("abastecimiento_horaing").setVisible(false);
        tabTabla.getColumna("abastecimiento_logining").setVisible(false);
        tabTabla.getColumna("abastecimiento_kilometraje").setVisible(false);
        tabTabla.getColumna("abastecimiento_tipo_ingreso").setVisible(false);
        tabTabla.getColumna("abastecimiento_tipo_medicion").setVisible(false);
        tabTabla.getColumna("abastecimiento_anio").setVisible(false);
        tabTabla.getColumna("abastecimiento_periodo").setVisible(false);
        tabTabla.getColumna("abastecimiento_horasmes").setVisible(false);
        tabTabla.setTipoFormulario(true);
        tabTabla.getGrid().setColumns(2);
        tabTabla.dibujar();
        PanelTabla pntt = new PanelTabla();
        pntt.setPanelTabla(tabTabla);

        Grupo gru = new Grupo();
        gru.getChildren().add(pntt);
        panOpcion.getChildren().add(gru);

    }

    private void limpiarPanel() {
        panOpcion.getChildren().clear();
    }

    public void limpiar() {
        autCompleta.limpiar();
        utilitario.addUpdate("autCompleta");
        limpiarPanel();
        utilitario.addUpdate("panOpcion");
    }

    public void filtrarSolicitud(SelectEvent evt) {
        limpiar();
        autCompleta.onSelect(evt);
        dibujarPantalla();
    }

    public void busPlaca() {
        TablaGenerica tabDato = aCombustible.getVehiculo(Integer.parseInt(tabTabla.getValor("mve_secuencial")));
        if (!tabDato.isEmpty()) {
            if (tabDato.getValor("placa").equals("0000000")) {
                tabTabla.getColumna("abastecimiento_kilometraje").setLectura(true);
                tabTabla.getColumna("abastecimiento_cod_dependencia").setLectura(false);
                tabTabla.getColumna("tipo_combustible_id").setLectura(false);
                tabTabla.setValor("abastecimiento_tipo_ingreso", "O");
                utilitario.addUpdate("tabTabla");
            } else {
                tabTabla.setValor("abastecimiento_cod_conductor", tabDato.getValor("cod_conductor"));
                tabTabla.setValor("tipo_combustible_id", tabDato.getValor("tipo_combustible_id"));
                tabTabla.setValor("abastecimiento_cod_dependencia", tabDato.getValor("departamento_pertenece"));
                tabTabla.getColumna("abastecimiento_kilometraje").setLectura(false);
                tabTabla.getColumna("abastecimiento_cod_dependencia").setLectura(true);
                tabTabla.getColumna("tipo_combustible_id").setLectura(true);
                tabTabla.setValor("abastecimiento_tipo_ingreso", "H");
                utilitario.addUpdate("tabTabla");
            }
        } else {
            utilitario.agregarMensajeError("Vehiculo", "No Se Encuentra Registrado");
        }
    }

    public void carga() {
        tabTabla.setValor("abastecimiento_anio", String.valueOf(utilitario.getAnio(tabTabla.getValor("abastecimiento_fecha"))));
        tabTabla.setValor("abastecimiento_periodo", String.valueOf(utilitario.getMes(tabTabla.getValor("abastecimiento_fecha"))));
        utilitario.addUpdate("tabTabla");
        TablaGenerica tabDato = aCombustible.getVehiculo(Integer.parseInt(tabTabla.getValor("mve_secuencial")));
        if (!tabDato.isEmpty()) {
            if (tabDato.getValor("placa").equals("0000000")) {
                valor();
            } else {
                Double valor1 = Double.valueOf(tabDato.getValor("capacidad_tanque"));
                Double valor2 = Double.valueOf(tabTabla.getValor("abastecimiento_galones"));
                if (valor2 <= valor1) {
                    valor();
                } else {
                    utilitario.agregarMensajeError("Galones", "Exceden Capacidad de Vehiculo");
                    tabTabla.setValor("abastecimiento_galones", null);
                    utilitario.addUpdate("tabTabla");
                }
            }
        } else {
            utilitario.agregarMensajeError("Valor", "No Se Encuentra Registrado");
        }
    }

    public void valor() {
        TablaGenerica tabDatos = aCombustible.getVehiculo(Integer.parseInt(tabTabla.getValor("mve_secuencial")));
        if (!tabDatos.isEmpty()) {
            if (tabDatos.getValor("placa").equals("0000000")) {
                TablaGenerica tabDato = aCombustible.getCombustible(Integer.parseInt(tabTabla.getValor("tipo_combustible_id")));
                if (!tabDato.isEmpty()) {
                    Double valor;
                    valor = (Double.parseDouble(tabDato.getValor("tipo_valor_galon")) * Double.parseDouble(tabTabla.getValor("abastecimiento_galones")));
                    tabTabla.setValor("abastecimiento_total", String.valueOf(Math.rint(valor * 100) / 100));
                    secuencial();
                } else {
                    utilitario.agregarMensajeError("Valor", "No Se Encuentra Registrado");
                }
            } else {
                String minutos = tabTabla.getValor("abastecimiento_valorhora").substring(3, 5);
                if (Integer.parseInt(minutos) < 59) {
                    TablaGenerica tabDato = aCombustible.getCombustible(Integer.parseInt(tabTabla.getValor("tipo_combustible_id")));
                    if (!tabDato.isEmpty()) {
                        Double valor;
                        valor = (Double.parseDouble(tabDato.getValor("tipo_valor_galon")) * Double.parseDouble(tabTabla.getValor("abastecimiento_galones")));
                        tabTabla.setValor("abastecimiento_total", String.valueOf(Math.rint(valor * 100) / 100));
                        secuencial();
                    } else {
                        utilitario.agregarMensajeError("Valor", "No Se Encuentra Registrado");
                    }
                } else {
                    utilitario.agregarMensaje("Minutos no deben ser menores a 60", "");
                }
            }
        } else {
            utilitario.agregarMensajeError("Valor", "No Se Encuentra Registrado");
        }
    }

    public void secuencial() {
        TablaGenerica tabDato = aCombustible.getVehiculo(Integer.parseInt(tabTabla.getValor("mve_secuencial")));
        if (!tabDato.isEmpty()) {
            if (tabDato.getValor("placa").equals("0000000")) {
                if (tabTabla.getValor("abastecimiento_fecha") != null && tabTabla.getValor("abastecimiento_fecha").toString().isEmpty() == false) {
                    if (tabTabla.getValor("abastecimiento_numero") != null && tabTabla.getValor("abastecimiento_numero").toString().isEmpty() == false) {
                    } else {
                        Integer cantidad = 0;
                        Integer numero = Integer.parseInt(aCombustible.listaMax(Integer.parseInt(tabTabla.getValor("abastecimiento_cod_dependencia")), String.valueOf(utilitario.getAnio(tabTabla.getValor("abastecimiento_fecha"))), String.valueOf(utilitario.getMes(tabTabla.getValor("abastecimiento_fecha")))));
                        cantidad = numero + 1;
                        tabTabla.setValor("abastecimiento_numero", String.valueOf(cantidad));
                        utilitario.addUpdate("tabTabla");
                    }
                } else {
                    tabTabla.setValor("abastecimiento_numero_vale", null);
                    utilitario.addUpdate("tabTabla");
                    utilitario.agregarMensaje("Ingresar Fecha de Abastecimiento", "");
                }
            } else {
                if (tabTabla.getValor("abastecimiento_fecha") != null && tabTabla.getValor("abastecimiento_fecha").toString().isEmpty() == false) {
                    if (tabTabla.getValor("abastecimiento_numero") != null && tabTabla.getValor("abastecimiento_numero").toString().isEmpty() == false) {
                    } else {
                        Integer numero = Integer.parseInt(aCombustible.listaMax(Integer.parseInt(tabTabla.getValor("mve_secuencial")), String.valueOf(utilitario.getAnio(tabTabla.getValor("abastecimiento_fecha"))), String.valueOf(utilitario.getMes(tabTabla.getValor("abastecimiento_fecha")))));
                        Integer cantidad = 0;
                        cantidad = numero + 1;
                        tabTabla.setValor("abastecimiento_numero", String.valueOf(cantidad));
                        utilitario.addUpdate("tabTabla");
                    }
                } else {
                    tabTabla.setValor("abastecimiento_numero_vale", null);
                    utilitario.addUpdate("tabTabla");
                    utilitario.agregarMensaje("Ingresar Fecha de Abastecimiento", "");
                }
            }
        } else {
            utilitario.agregarMensajeError("Valor", "No Se Encuentra Registrado");
        }
    }

    public void abrirCuadro() {
        setTabla.dibujar();
    }

    public void aceptarRegistro() {
        if (calFechaInicio.getValue() != null && calFechaFin.getValue() != null) {
            setTabla.getTab_seleccion().setSql("SELECT a.abastecimiento_id, \n"
                    + "a.abastecimiento_fecha, \n"
                    + "a.abastecimiento_numero_vale, \n"
                    + "(case when a.mve_secuencial is not null then (case when v.placa is not null then v.placa when v.placa is null then v.codigo_activo end )  \n"
                    + "when a.mve_secuencial is null then d.dependencia_descripcion end )  as descripcion \n"
                    + "FROM mvabactecimiento_combustible AS a \n"
                    + "left JOIN mv_vehiculo v ON a.mve_secuencial = v.mve_secuencial  \n"
                    + "left join mvtipo_dependencias d on a.abastecimiento_cod_dependencia = d.dependencia_codigo \n"
                    + "WHERE a.abastecimiento_tipo_ingreso = 'H'\n"
                    + "and a.abastecimiento_fecha BETWEEN '" + calFechaInicio.getFecha() + "'and'" + calFechaFin.getFecha() + "'\n"
                    + "ORDER BY a.abastecimiento_fecha,a.abastecimiento_numero_vale");
            setTabla.getTab_seleccion().ejecutarSql();
        } else {
            utilitario.agregarMensajeInfo("Debe seleccionar un rago de fechas", "");
        }
    }

    public void buscarRegistro() {
        if (setTabla.getValorSeleccionado() != null) {
            autCompleta.setValor(setTabla.getValorSeleccionado());
            dibujarPantalla();
            setTabla.cerrar();
            utilitario.addUpdate("autCompleta,panOpcion");
        } else {
            utilitario.agregarMensajeInfo("Debe seleccionar una Solicitud", "");
        }
    }

    public void ingDependencia() {
        dialogoa.Limpiar();
        dialogoa.setDialogo(grida);
        grid.getChildren().add(setDepencias);
        setDepencias.setId("setDepencias");
        setDepencias.setConexion(conPostgres);
        setDepencias.setSql("SELECT dependencia_codigo,dependencia_descripcion from mvtipo_dependencias order by dependencia_descripcion");
        setDepencias.getColumna("dependencia_descripcion").setFiltro(true);
        setDepencias.setRows(9);
        setDepencias.setTipoSeleccion(false);
        dialogoa.setDialogo(grid);
        setDepencias.dibujar();
        dialogoa.dibujar();
    }

    public void insAccesorio() {
        if (taccesorio.getValue() != null && taccesorio.toString().isEmpty() == false) {
            aCombustible.setDependencias(taccesorio.getValue() + "");
            taccesorio.limpiar();
            utilitario.agregarMensaje("Registro Guardado", "Accesorio");
            setDepencias.actualizar();
        }
    }

    public void endAccesorio() {
        if (setDepencias.getValorSeleccionado() != null && setDepencias.getValorSeleccionado().isEmpty() == false) {
            aCombustible.deleteDependencias(Integer.parseInt(setDepencias.getValorSeleccionado()));
            utilitario.agregarMensaje("Registro eliminado", "Accesorio");
            setDepencias.actualizar();
        } else {
            utilitario.agregarMensajeInfo("Debe seleccionar al menos un registro", "");
        }
    }

    public void horaActu() {
        Integer valor = 0, vt_hora;
        String num;
        TablaGenerica tabDato = aCombustible.getHorav(Integer.parseInt(tabTabla.getValor("mve_secuencial")));
        if (!tabDato.isEmpty()) {
            String cadena;
            String horaa = tabTabla.getValor("abastecimiento_valorhora").substring(0, 2);
            String minutos = tabTabla.getValor("abastecimiento_valorhora").substring(3, 5);
            String horas, minutos1;
            if (tabDato.getValor("horometro") != null) {
                horas = tabDato.getValor("horometro").substring(0, 4);
                minutos1 = tabDato.getValor("horometro").substring(5, 7);
            } else {
                horas = "0";
                minutos1 = "0";
            }
            Integer suma = Integer.parseInt(minutos) + Integer.parseInt(minutos1);

            if (suma > 60) {
                valor = suma - 60;
                vt_hora = Integer.parseInt(horas) + 1 + Integer.parseInt(horaa);
                if (vt_hora >= 0 && vt_hora <= 9) {
                    num = "000" + String.valueOf(vt_hora);
                    if (valor >= 10) {
                        cadena = num + ":" + String.valueOf(valor);
                    } else {
                        cadena = num + ":0" + String.valueOf(valor);
                    }
                    tabTabla.setValor("abastecimiento_horasmes", cadena);
                } else if (vt_hora >= 10 && vt_hora <= 99) {
                    num = "00" + String.valueOf(vt_hora);
                    if (valor >= 10) {
                        cadena = num + ":" + String.valueOf(valor);
                    } else {
                        cadena = num + ":0" + String.valueOf(valor);
                    }
                    tabTabla.setValor("abastecimiento_horasmes", cadena);
                } else if (vt_hora >= 100 && vt_hora <= 999) {
                    num = "0" + String.valueOf(vt_hora);
                    if (valor >= 10) {
                        cadena = num + ":" + String.valueOf(valor);
                    } else {
                        cadena = num + ":0" + String.valueOf(valor);
                    }
                    tabTabla.setValor("abastecimiento_horasmes", cadena);
                } else if (vt_hora >= 1000 && vt_hora <= 9999) {
                    num = String.valueOf(vt_hora);
                    if (valor >= 10) {
                        cadena = num + ":" + String.valueOf(valor);
                    } else {
                        cadena = num + ":0" + String.valueOf(valor);
                    }
                    tabTabla.setValor("abastecimiento_horasmes", cadena);
                }
            } else if (suma == 60) {
                vt_hora = Integer.parseInt(horas) + 1 + Integer.parseInt(horaa);
                if (vt_hora >= 0 && vt_hora <= 9) {
                    num = "000" + String.valueOf(vt_hora);
                    cadena = num + ":" + String.valueOf(valor);
                    tabTabla.setValor("abastecimiento_horasmes", cadena);
                } else if (vt_hora >= 10 && vt_hora <= 99) {
                    num = "00" + String.valueOf(vt_hora);
                    cadena = num + ":" + "00";
                    tabTabla.setValor("abastecimiento_horasmes", cadena);
                } else if (vt_hora >= 100 && vt_hora <= 999) {
                    num = "0" + String.valueOf(vt_hora);
                    cadena = num + ":" + "00";
                    tabTabla.setValor("abastecimiento_horasmes", cadena);
                } else if (vt_hora >= 1000 && vt_hora <= 9999) {
                    num = String.valueOf(vt_hora);
                    cadena = num + ":" + "00";
                    tabTabla.setValor("abastecimiento_horasmes", cadena);
                }
            } else if (suma < 60) {
                valor = Integer.parseInt(minutos) + Integer.parseInt(minutos1);
                vt_hora = Integer.parseInt(horas) + Integer.parseInt(horaa);
                if (vt_hora >= 0 && vt_hora <= 9) {
                    num = "000" + String.valueOf(vt_hora);
                    if (valor >= 10) {
                        cadena = num + ":" + String.valueOf(valor);
                    } else {
                        cadena = num + ":0" + String.valueOf(valor);
                    }
                    tabTabla.setValor("abastecimiento_horasmes", cadena);
                } else if (vt_hora >= 10 && vt_hora <= 99) {
                    num = "00" + String.valueOf(vt_hora);
                    if (valor >= 10) {
                        cadena = num + ":" + String.valueOf(valor);
                    } else {
                        cadena = num + ":0" + String.valueOf(valor);
                    }
                    tabTabla.setValor("abastecimiento_horasmes", cadena);
                } else if (vt_hora >= 100 && vt_hora <= 999) {
                    num = "0" + String.valueOf(vt_hora);
                    if (valor >= 10) {
                        cadena = num + ":" + String.valueOf(valor);
                    } else {
                        cadena = num + ":0" + String.valueOf(valor);
                    }
                    tabTabla.setValor("abastecimiento_horasmes", cadena);
                } else if (vt_hora >= 1000 && vt_hora <= 9999) {
                    num = String.valueOf(vt_hora);
                    if (valor >= 10) {
                        cadena = num + ":" + String.valueOf(valor);
                    } else {
                        cadena = num + ":0" + String.valueOf(valor);
                    }
                    tabTabla.setValor("abastecimiento_horasmes", cadena);
                }
            }
            utilitario.addUpdate("tabTabla");
        } else {
            utilitario.agregarMensaje("no encuentra datos hora", "");
        }
    }

    @Override
    public void insertar() {
        utilitario.getTablaisFocus().insertar();
    }

    @Override
    public void guardar() {
        if (tabTabla.getValor("abastecimiento_id") != null) {
            TablaGenerica tabInfo = bCombustible.getCatalogoDato("*", tabTabla.getTabla(), "abastecimiento_id = " + tabTabla.getValor("abastecimiento_id") + "");
            if (!tabInfo.isEmpty()) {
                TablaGenerica tabDato = bCombustible.getNumeroCampos(tabTabla.getTabla());
                if (!tabDato.isEmpty()) {
                    for (int i = 1; i < Integer.parseInt(tabDato.getValor("NumeroCampos")); i++) {
                        if (i != 1) {
                            TablaGenerica tabInfoColum1 = bCombustible.getEstrucTabla(tabTabla.getTabla(), i);
                            if (!tabInfoColum1.isEmpty()) {
                                try {
                                    if (tabTabla.getValor(tabInfoColum1.getValor("Column_Name")).equals(tabInfo.getValor(tabInfoColum1.getValor("Column_Name")))) {
                                    } else {
                                        bCombustible.setActuaRegis(Integer.parseInt(tabTabla.getValor("abastecimiento_id")), tabTabla.getTabla(), tabInfoColum1.getValor("Column_Name"), tabTabla.getValor(tabInfoColum1.getValor("Column_Name")), "abastecimiento_id");
                                    }
                                } catch (NullPointerException e) {
                                }
                            }
                        }
                    }
                }
            }
            utilitario.agregarMensaje("Registro Actalizado", null);
        } else {
            if (tabTabla.guardar()) {
                conPostgres.guardarPantalla();
            }
        }
        horaActu1();
    }

    @Override
    public void eliminar() {
    }

    public void horaActu1() {
        TablaGenerica tabDato = aCombustible.getVehiculo(Integer.parseInt(tabTabla.getValor("mve_secuencial")));
        if (!tabDato.isEmpty()) {
            if (tabDato.getValor("placa").equals("0000000")) {
            } else {
                aCombustible.set_ActuaHR(Integer.parseInt(tabTabla.getValor("mve_secuencial")), tabTabla.getValor("abastecimiento_horasmes"), "horometro");
            }
        } else {
            utilitario.agregarMensajeError("Valor", "No Se Encuentra Registrado");
        }
    }

    public Conexion getConPostgres() {
        return conPostgres;
    }

    public void setConPostgres(Conexion conPostgres) {
        this.conPostgres = conPostgres;
    }

    public Tabla getTabTabla() {
        return tabTabla;
    }

    public void setTabTabla(Tabla tabTabla) {
        this.tabTabla = tabTabla;
    }

    public Tabla getSetDepencias() {
        return setDepencias;
    }

    public void setSetDepencias(Tabla setDepencias) {
        this.setDepencias = setDepencias;
    }

    public SeleccionTabla getSetTabla() {
        return setTabla;
    }

    public void setSetTabla(SeleccionTabla setTabla) {
        this.setTabla = setTabla;
    }

    public AutoCompletar getAutCompleta() {
        return autCompleta;
    }

    public void setAutCompleta(AutoCompletar autCompleta) {
        this.autCompleta = autCompleta;
    }
}
