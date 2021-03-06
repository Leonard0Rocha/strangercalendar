package br.ufg.inf.mds.strangecalendar.view;

import br.ufg.inf.mds.strangecalendar.controller.EventoController;
import br.ufg.inf.mds.strangecalendar.controller.InteressadoController;
import br.ufg.inf.mds.strangecalendar.controller.RegionalController;
import br.ufg.inf.mds.strangecalendar.entidade.Evento;
import br.ufg.inf.mds.strangecalendar.entidade.Interessado;
import br.ufg.inf.mds.strangecalendar.entidade.Regional;
import br.ufg.inf.mds.strangecalendar.services.exceptions.ServicoException;
import br.ufg.inf.mds.strangecalendar.util.Leitura;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 *
 * @author Leonardo
 */
public class ViewCadastrarEventos {

    private static final Logger LOG = LoggerFactory
    		.getLogger(ViewCadastrarEventos.class);

    private Scanner scanner;
    private EventoController eventoController;
	private InteressadoController interessadoController;
	private RegionalController regionalController;

    public ViewCadastrarEventos(Scanner scanner, ApplicationContext context) {
    	this.scanner = scanner;
    	this.eventoController = context.getBean(EventoController.class);
		this.interessadoController = context
				.getBean(InteressadoController.class);
		this.regionalController = context.getBean(RegionalController.class);
    }

    public void exibirCadastroEvento() {
        System.out.println("##### Bem Vindo ao Cadastro de Evento #####\n");

        if (naoPossuiRegionaisCadastradas()) {
            System.out.println("Você não possui nenhuma regional cadastrada "
                    + "ainda. É necessário cadastrar pelo menos uma Regional "
                    + "para poder Cadastrar um Evento");
            return;
        }

        String descricao = Leitura.lerCampoStringObrigatorio("Informe a "
                + "descrição do Evento", getScanner());

        LocalDateTime dataInicio = Leitura.lerCampoDateTimeObrigatorio
                ("Informe a data/hora "
                + "de início do Evento (Formato: dd/MM/yyyy hh:mm)", getScanner());

        LocalDateTime dataFim = Leitura.lerCampoDateTimeObrigatorio("Informe" +
                " a data/hora de "
                + "término do Evento  (Formato: dd/MM/yyyy hh:mm)", getScanner());

        Evento evento = new Evento();
        evento = popularObjetoEvento(evento, descricao, dataInicio, dataFim,
                adicionarRegional(evento), adicionarInteressado(evento));

        inserirEvento(evento);
    }

    private boolean naoPossuiRegionaisCadastradas() {
        List<Regional> listRegionais = regionalController.listarRegionais();

        return listRegionais.isEmpty();
    }

    private Evento popularObjetoEvento(Evento evento, String descricao,
            LocalDateTime dataInicio, LocalDateTime dataFim, Set<Regional>
                                               listRegional,
            Set<Interessado>listInteressados) {

        evento.setDescricao(descricao);
        evento.setDataInicio(dataInicio);
        evento.setDataFim(dataFim);
        evento.setRegionais(listRegional);
        evento.setInteressados(listInteressados);

        return evento;
    }

    private void inserirEvento(Evento evento) {
        try {
            eventoController.cadastrarEvento(evento);
            System.out.println("\n##### Evento cadastrado com sucesso #####");
        } catch (ServicoException ex) {
            System.out.println("\nNão foi possível cadastrar o Evento. Motivo:"
                    + ex.getMessage());
			LOG.trace(ex.getMessage(), ex);}
    }

    private Set<Regional> adicionarRegional(Evento evento) {
        List<Regional> listRegionaisCadastradas = regionalController
        		.listarRegionais();
        Set<Regional> regionaisEscolhidas = new LinkedHashSet<>();
        Map<Long, String> mapRegionais = new LinkedHashMap<>();
        mapRegionais = populaMapRegionais(mapRegionais,
                listRegionaisCadastradas);
        boolean adicionarRegional = true;

        while (adicionarRegional) {
            regionaisEscolhidas = adicionarRegionalSelecionadaNaList(
                    mapRegionais, regionaisEscolhidas,
                    listRegionaisCadastradas);

            adicionarRegional = Leitura.lerCampoBooleanObrigatorio("Deseja "
                    + "adicionar mais uma Regional? Digite 1 para SIM "
                    + "e 0 para NÃO.", getScanner());
        }

        return regionaisEscolhidas;
    }

    private Set<Interessado> adicionarInteressado(Evento evento) {
        List<Interessado> listInteressadosCadastradas = interessadoController
        		.listarInteressados();
        Set<Interessado> interessadosEscolhidas = new LinkedHashSet<>();
        boolean adicionarInteressado = true;

        while (adicionarInteressado) {
            interessadosEscolhidas = adicionarInteressadoSelecionadaNaList(
                    interessadosEscolhidas, listInteressadosCadastradas);

            adicionarInteressado = Leitura.lerCampoBooleanObrigatorio("Deseja "
                    + "adicionar mais um Interessado? Digite 1 para SIM "
                    + "e 0 para NÃO.", getScanner());
        }

        return interessadosEscolhidas;
    }

    private Map<Long, String> populaMapRegionais(Map<Long, String> mapRegionais,
            List<Regional> listRegionaisCadastradas) {

        for (Regional regional : listRegionaisCadastradas) {
            mapRegionais.put(regional.getId(), regional.getNome());
        }
        return mapRegionais;
    }

    private Set<Regional> adicionarRegionalSelecionadaNaList(
            Map<Long, String> mapRegionais, Set<Regional> regionaisEscolhidas,
            List<Regional> listRegionaisCadastradas) {

        int idRegional = 0;
        do {
            System.out.println("Selecione a regional onde esse evento irá "
                    + "acontecer informando o número correspondente:");
            for (Map.Entry<Long, String> regional : mapRegionais.entrySet()) {
                System.out.println(regional.getKey() + " - "
                        + regional.getValue());
            }
            try {
                idRegional = Integer.parseInt(getScanner().nextLine());
                if (!(buscarRegionalCadastrada(mapRegionais, idRegional))) {
                    System.out.println("Número informado não corresponde a "
                            + "nenhuma Regional");
                }
            } catch (NumberFormatException ex) {
                System.out.println("Entrada inválida. Informe um número inteiro"
                        + " correspondente a Regional");
            }
        } while (!buscarRegionalCadastrada(mapRegionais, idRegional));
        regionaisEscolhidas.add(obterRegionalPeloId(mapRegionais, idRegional));

        return regionaisEscolhidas;
    }

	private Regional obterRegionalPeloId(Map<Long, String> mapRegionais, int idRegional) {
		Regional regional = new Regional();
        long idRegionalLong = idRegional;
        String nomeRegional = mapRegionais.get(idRegionalLong);
        regional.setId(idRegionalLong);
        regional.setNome(nomeRegional);
        
		return regional;
	}

	private boolean buscarRegionalCadastrada(Map<Long, String> mapRegionais, 
			Integer idRegional) {
		
		    boolean existeRegional = false;
            long idRegionalLong = idRegional;
            String value = mapRegionais.get(idRegionalLong);
            
            if(value != null){
            	existeRegional = true;
            }

		return existeRegional;
	}

	private Set<Interessado> adicionarInteressadoSelecionadaNaList(
            Set<Interessado> interessadosEscolhidos,
            List<Interessado> listInteressadosCadastrados) {

        int idInteressado = selecionarInteressado(listInteressadosCadastrados);

        interessadosEscolhidos.add(listInteressadosCadastrados.
                get(idInteressado - 1));
        return interessadosEscolhidos;
    }

    private int selecionarInteressado(List<Interessado> listInteressados) {
		int idInteressado = 0;
        do {
            System.out.println("Selecione o interessado no evento informando o"
                    + " número correspondente:");
            for (Interessado interessado : listInteressados) {
                System.out.println(interessado.getId() + " - "
                		+ interessado.getNome());
            }
            try {
                idInteressado = Integer.parseInt(getScanner().nextLine());
                if (idInteressado < 1
                		|| idInteressado > listInteressados.size()) {
                    System.out.println("Número informado não corresponde a "
                            + "nenhum Interessado");
                }
            } catch (NumberFormatException ex) {
                System.out.println("Entrada inválida. Informe um número inteiro"
                        + " correspondente ao Interessado");
            }
        } while (idInteressado < 1 || idInteressado > listInteressados.size());
		return idInteressado;
	}

    public Scanner getScanner() {
		return scanner;
	}

	public void setScanner(Scanner scanner) {
		this.scanner = scanner;
	}

}
