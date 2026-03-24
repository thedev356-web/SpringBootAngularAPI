package com.ict.tender_service.repository;

import com.ict.tender_service.model.Tender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TenderRepository extends JpaRepository<Tender, Long> {

    List<Tender> findByDistrict(String district);

    List<Tender> findByCategory(String category);

    List<Tender> findByDistrictAndCategory(String district, String category);

    List<Tender> findByStatus(Tender.TenderStatus status);

    List<Tender> findByOrganization(String organization);
}