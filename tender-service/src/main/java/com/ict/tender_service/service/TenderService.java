package com.ict.tender_service.service;

import com.ict.tender_service.model.Tender;
import com.ict.tender_service.repository.TenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TenderService {

    @Autowired
    private TenderRepository tenderRepository;

    // Get all tenders
    public List<Tender> getAllTenders() {
        return tenderRepository.findAll();
    }

    // Get tender by ID
    public Optional<Tender> getTenderById(Long id) {
        return tenderRepository.findById(id);
    }

    // Get tenders by district
    public List<Tender> getTendersByDistrict(String district) {
        return tenderRepository.findByDistrict(district);
    }

    // Get tenders by category
    public List<Tender> getTendersByCategory(String category) {
        return tenderRepository.findByCategory(category);
    }

    // Get tenders by district and category
    public List<Tender> getTendersByDistrictAndCategory(
            String district, String category) {
        return tenderRepository.findByDistrictAndCategory(district, category);
    }

    // Get active tenders
    public List<Tender> getActiveTenders() {
        return tenderRepository.findByStatus(Tender.TenderStatus.ACTIVE);
    }

    // Save tender
    public Tender saveTender(Tender tender) {
        return tenderRepository.save(tender);
    }

    // Update tender
    public Tender updateTender(Long id, Tender tender) {
        tender.setId(id);
        return tenderRepository.save(tender);
    }

    // Delete tender
    public void deleteTender(Long id) {
        tenderRepository.deleteById(id);
    }
}