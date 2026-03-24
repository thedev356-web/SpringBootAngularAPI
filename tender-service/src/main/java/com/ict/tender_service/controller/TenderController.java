package com.ict.tender_service.controller;

import com.ict.tender_service.model.Tender;
import com.ict.tender_service.repository.TenderRepository;
import com.ict.tender_service.service.TenderService;
import com.ict.tender_service.service.SeleniumScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tenders")
@CrossOrigin(origins = "https://ict-construction.vercel.app")
public class TenderController {

    @Autowired
    private TenderService tenderService;

    @Autowired
    private SeleniumScraperService scraperService;

    @Autowired
    private TenderRepository tenderRepository;

    @GetMapping
    public List<Tender> getAllTenders() {
        return tenderService.getAllTenders();
    }

    @GetMapping("/active")
    public List<Tender> getActiveTenders() {
        return tenderService.getActiveTenders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tender> getTenderById(@PathVariable Long id) {
        return tenderService.getTenderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/district/{district}")
    public List<Tender> getTendersByDistrict(@PathVariable String district) {
        return tenderService.getTendersByDistrict(district);
    }

    @GetMapping("/category/{category}")
    public List<Tender> getTendersByCategory(@PathVariable String category) {
        return tenderService.getTendersByCategory(category);
    }

    @GetMapping("/filter")
    public List<Tender> filterTenders(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String category) {
        if (district != null && category != null) {
            return tenderService.getTendersByDistrictAndCategory(
                    district, category);
        } else if (district != null) {
            return tenderService.getTendersByDistrict(district);
        } else if (category != null) {
            return tenderService.getTendersByCategory(category);
        }
        return tenderService.getAllTenders();
    }

    @PostMapping
    public Tender createTender(@RequestBody Tender tender) {
        return tenderService.saveTender(tender);
    }

    @PutMapping("/{id}")
    public Tender updateTender(@PathVariable Long id,
                               @RequestBody Tender tender) {
        return tenderService.updateTender(id, tender);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTender(@PathVariable Long id) {
        tenderService.deleteTender(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/scrape")
    public ResponseEntity<String> scrapeNow() {
        new Thread(() -> scraperService.scrapeNow()).start();
        return ResponseEntity.ok(
                "Scraping started! Check /api/tenders in 1-2 minutes.");
    }

    // Paginated tenders
    @GetMapping("/page")
    public ResponseEntity<?> getTendersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String category) {

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by("createdAt").descending());

        org.springframework.data.domain.Page<Tender> result =
                tenderRepository.findAll(pageable);

        return ResponseEntity.ok(result);
    }

}