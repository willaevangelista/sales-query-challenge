package com.devsuperior.dsmeta.repositories;

import com.devsuperior.dsmeta.dto.SaleReportDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.devsuperior.dsmeta.entities.Sale;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query(value = "SELECT new com.devsuperior.dsmeta.dto.SaleReportDTO(s.id, s.date, s.amount, s.seller.name) "
            + "FROM Sale s "
            + "WHERE s.date BETWEEN :minDate AND :maxDate "
            + "AND LOWER(s.seller.name) LIKE LOWER(CONCAT('%', :name, '%'))",
            countQuery = "SELECT COUNT(s) "
                    + "FROM Sale s "
                    + "WHERE s.date BETWEEN :minDate AND :maxDate "
                    + "AND LOWER(s.seller.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<SaleReportDTO> findSalesReport(@Param("minDate") LocalDate minDate,
                                        @Param("maxDate") LocalDate maxDate,
                                        @Param("name") String name,
                                        Pageable pageable);

}
