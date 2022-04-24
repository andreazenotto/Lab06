package it.polito.tdp.meteo.model;

import java.util.LinkedList;
import java.util.List;

import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	
	MeteoDAO meteoDAO;
	
	private int COST = 100;
	private int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private int NUMERO_GIORNI_CITTA_MAX = 6;
	private int NUMERO_GIORNI_TOTALI = 15;
	private int minimo;
	private List<Rilevamento> percorso;

	public Model() {
		this.meteoDAO = new MeteoDAO();
		this.percorso = new LinkedList<>();
	}

	// of course you can change the String output with what you think works best
	public String getUmiditaMedia(int mese) {
		Citta torino = new Citta("Torino");
		Citta genova = new Citta("Genova");
		Citta milano = new Citta("Milano");
		for(Rilevamento r: this.meteoDAO.getAllRilevamenti()) {
			if(r.getLocalita().equals(torino.getNome()))
				torino.addRilevamento(r);
			if(r.getLocalita().equals(genova.getNome()))
				genova.addRilevamento(r);
			if(r.getLocalita().equals(milano.getNome()))
				milano.addRilevamento(r);
		}
		return "Torino, umidità media: " + torino.umiditaMediaPerMese(mese) + "\n" +
			   "Genova, umidità media: " + genova.umiditaMediaPerMese(mese) + "\n" +
			   "Milano, umidità media: " + milano.umiditaMediaPerMese(mese);
	}
	
	// of course you can change the String output with what you think works best
	public String trovaSequenza(int mese) {
		this.minimo = 1000000000;
		this.percorso.clear();
		List<Rilevamento> rilevamenti = this.meteoDAO.getAllRilevamentiMese(mese);
		List<Rilevamento> parziale = new LinkedList<>();
		this.ricorsione(parziale, rilevamenti, 1);
		String stringa = "";
		for(Rilevamento r: this.percorso) {
			if(stringa!="")
				stringa += "\n";
			stringa += r.toString();
		}
		return stringa;
	}
	
	public void ricorsione(List<Rilevamento> parziale, List<Rilevamento> rilevamenti, int livello) {
		if(parziale.size()==this.NUMERO_GIORNI_TOTALI) {
			// controllo vincolo tutte città visitate almeno una volta
			if(this.controlloTutteCittaRilevate(parziale)) {
				int count = 0;
				for(int i=0; i<parziale.size(); i++) {
					if(i>0) {
						if(!parziale.get(i).getLocalita().equals(parziale.get(i-1).getLocalita())) {
							count += this.COST;
						}
					}
					count += parziale.get(i).getUmidita();
				}
				if(count<minimo) {
					minimo = count;
					this.percorso.clear();
					this.percorso.addAll(parziale);
				}
			}
			return;
		}
		
		for(Rilevamento r: rilevamenti) {
			// se il rilevamento è relativo al giorno successivo all'ultimo già considerato allora mi interessa
			if(r.getData().getDayOfMonth()==livello) {
				parziale.add(r);
				if(this.controlloVincoliRilevamento(parziale))
					this.ricorsione(parziale, rilevamenti, livello+1);
				parziale.remove(parziale.size()-1);
			}
		}
	}

	private boolean controlloVincoliRilevamento(List<Rilevamento> parziale) {
		if(parziale.size()==1)
			return true;
		// se la rilevazione precedente(r1) rispetto a quella appena aggiunta(r2) è di
		// un'altra località allora devo controllare che almeno le ultime tre rilevazioni,
		// prima di r2, contenute in parziale siano state fatte nella stessa località di r1
		if(!parziale.get(parziale.size()-1).getLocalita().equals(parziale.get(parziale.size()-2).getLocalita())) {
			if(parziale.size()>this.NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN) {
				for(int i=1; i<this.NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN; i++) {
					if(!parziale.get(parziale.size()-(2+i)).getLocalita().equals(parziale.get(parziale.size()-2).getLocalita()))
						return false;
				}
			} else {
				return false;
			}
		}
		
		int countTorino = 0;
		int countGenova = 0;
		int countMilano = 0;
		for(Rilevamento r: parziale) {
			if(r.getLocalita().toLowerCase().equals("torino"))
				countTorino++;
			else if(r.getLocalita().toLowerCase().equals("genova"))
				countGenova++;
			else if(r.getLocalita().toLowerCase().equals("milano"))
				countMilano++;
		}
		if(countTorino>this.NUMERO_GIORNI_CITTA_MAX || 
		   countGenova>this.NUMERO_GIORNI_CITTA_MAX || 
		   countMilano>this.NUMERO_GIORNI_CITTA_MAX)
			return false;
		return true;
	}
	
	private boolean controlloTutteCittaRilevate(List<Rilevamento> parziale) {
		boolean torino = false;
		boolean genova = false;
		boolean milano = false;
		for(Rilevamento r: parziale) {
			if(r.getLocalita().toLowerCase().equals("torino"))
				torino = true;
			else if(r.getLocalita().toLowerCase().equals("genova"))
				genova = true;
			else if(r.getLocalita().toLowerCase().equals("milano"))
				milano = true;
		}
		if(torino && genova && milano)
			return true;
		return false;
	}

}
