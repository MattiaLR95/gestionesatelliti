package it.prova.gestionesatelliti.web.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.prova.gestionesatelliti.model.Satellite;
import it.prova.gestionesatelliti.model.Stato;
import it.prova.gestionesatelliti.service.SatelliteService;

@Controller
@RequestMapping(value = "/satellite")
public class SatelliteController {

	@Autowired
	private SatelliteService satelliteService;

	@GetMapping
	public ModelAndView listAll() {
		ModelAndView mv = new ModelAndView();
		List<Satellite> results = satelliteService.listAllElements();
		mv.addObject("satellite_list_attribute", results);
		mv.setViewName("satellite/list");
		return mv;
	}

	@GetMapping("/search")
	public String search() {
		return "satellite/search";
	}

	@PostMapping("/list")
	public String listByExample(Satellite example, ModelMap model) {
		List<Satellite> results = satelliteService.findByExample(example);
		model.addAttribute("satellite_list_attribute", results);
		return "satellite/list";
	}

	@GetMapping("/insert")
	public String create(Model model) {
		model.addAttribute("insert_satellite_attr", new Satellite());
		return "satellite/insert";
	}

	@PostMapping("/save")
	public String save(@Valid @ModelAttribute("insert_satellite_attr") Satellite satellite, BindingResult result,
			RedirectAttributes redirectAttrs, Model model) {

		if (result.hasErrors()) {
			return "satellite/insert";
		}

		if (satellite.getDataDiLancio() != null) {
			if (satellite.getDataRientro() != null) {
				if (satellite.getDataDiLancio().after(satellite.getDataRientro())) {
					result.rejectValue("dataDiLancio", "error.data");
					return "satellite/insert";
				}
			}
		}

		if (satellite.getDataDiLancio() != null) {
			if (satellite.getDataDiLancio().before(new Date())) {
				if (satellite.getStato() == null) {
					result.rejectValue("stato", "error.data.stato");
					return "satellite/insert";
				}
			}
		}

		if (satellite.getDataDiLancio() == null) {
			if (satellite.getDataRientro() != null) {
				result.rejectValue("stato", "error.data.dataRientro");
				return "satellite/insert";
			}
		}

		if (satellite.getDataDiLancio().after(new Date())) {
			if (satellite.getStato() != null) {
				result.rejectValue("stato", "error.data.stato1");
				return "satellite/insert";
			}
		}

		satelliteService.inserisciNuovo(satellite);

		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/satellite";
	}

	@GetMapping("/show/{idSatellite}")
	public String show(@PathVariable(required = true) Long idSatellite, Model model) {
		model.addAttribute("show_satellite_attr", satelliteService.caricaSingoloElemento(idSatellite));
		return "satellite/show";
	}

	@GetMapping("/delete/{idSatellite}")
	public String delete(@PathVariable(required = true) Long idSatellite, Model model) {
		model.addAttribute("delete_satellite_attr", satelliteService.caricaSingoloElemento(idSatellite));
		return "satellite/delete";
	}

	@PostMapping("/savedelete/{idSatellite}")
	public String saveDelete(@PathVariable(required = true) Long idSatellite, RedirectAttributes redirectAttrs) {

		Satellite satelliteReloaded = satelliteService.caricaSingoloElemento(idSatellite);
		
		if(satelliteReloaded.getDataDiLancio().before(new Date())) {
			if(satelliteReloaded.getDataRientro().after(new Date())) {
				redirectAttrs.addFlashAttribute("errorMessage","Impossibile eliminare");
				return "redirect:/satellite";
			}
		}

		satelliteService.rimuovi(idSatellite);
		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/satellite";
	}

	@GetMapping("/update/{idSatellite}")
	public String update(@PathVariable(required = true) Long idSatellite, Model model) {
		model.addAttribute("update_satellite_attr", satelliteService.caricaSingoloElemento(idSatellite));
		return "satellite/update";
	}

	@PostMapping("/saveupdate")
	public String saveUpdate(@Valid @ModelAttribute("update_satellite_attr") Satellite satellite, BindingResult result,
			RedirectAttributes redirectAttrs) {

		if (result.hasErrors())
			return "satellite/update";
		
		if(satellite.getDataDiLancio()!=null && satellite.getDataDiLancio().before(new Date())) {
			result.rejectValue("dataDiLancio", "error.data.dataUpdate");
			return "satellite/update";
		}
		
		if(satellite.getDataRientro()!=null && satellite.getDataRientro().before(new Date())) {
			result.rejectValue("dataRientro", "error.data.dataUpdate");
			return "satellite/update";
		}
		
		if(satellite.getDataRientro()!=null && satellite.getDataRientro().after(new Date())) {
			if(satellite.getStato() == null || satellite.getStato()==Stato.DISATTIVATO) {
				result.rejectValue("stato", "error.data.statoUpdate");
				return "satellite/update";
			}
		}
		
		satelliteService.aggiorna(satellite);

		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/satellite";
	}

	@PostMapping("/lancia")
	public String lancia(@RequestParam(name = "idSatellite") Long idSatellite, ModelMap model) {
		Satellite satellite = satelliteService.caricaSingoloElemento(idSatellite);
		satellite.setDataDiLancio(new Date());
		satelliteService.aggiorna(satellite);
		model.addAttribute("todayDate_attr", new Date());
		model.addAttribute("satellite_list_attribute", satelliteService.listAllElements());
		return "satellite/list";
	}

	@PostMapping("/rientro")
	public String rientro(@RequestParam(name = "idSatellite") Long idSatellite, ModelMap model) {
		Satellite satellite = satelliteService.caricaSingoloElemento(idSatellite);
		if (satellite.getDataDiLancio() != null) {
			satellite.setDataRientro(new Date());
			satellite.setStato(Stato.DISATTIVATO);
			satelliteService.aggiorna(satellite);
		}
		model.addAttribute("todayDate_attr", new Date());
		model.addAttribute("satellite_list_attribute", satelliteService.listAllElements());
		return "satellite/list";
	}

	@GetMapping("/listDueAnni")
	public ModelAndView listAllDueAnni() {
		ModelAndView mv = new ModelAndView();

		Date dataProva = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dataProva);
		calendar.add(Calendar.YEAR, -2);
		Date dataMenoDue = calendar.getTime();
		List<Satellite> results = satelliteService.trovaSatellitiDueAnni(dataMenoDue);
		mv.addObject("satellite_list_attribute", results);
		mv.setViewName("satellite/list");
		return mv;
	}

	@GetMapping("/listDisattivati")
	public ModelAndView listAllDisattivati() {
		ModelAndView mv = new ModelAndView();
		List<Satellite> results = satelliteService.trovaSatellitiNonRientrati();
		mv.addObject("satellite_list_attribute", results);
		mv.setViewName("satellite/list");
		return mv;
	}

	@GetMapping("/listOrbita")
	public ModelAndView listAllDieciAnniOrbita() {
		ModelAndView mv = new ModelAndView();

		Date dataProva = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dataProva);
		calendar.add(Calendar.YEAR, -10);
		Date dataMenoDue = calendar.getTime();
		List<Satellite> results = satelliteService.trovaSatellitiDieciAnniOrbita(dataMenoDue);
		mv.addObject("satellite_list_attribute", results);
		mv.setViewName("satellite/list");
		return mv;
	}
}
