package com.devsuperior.dsmeta.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import com.devsuperior.dsmeta.dto.SaleReportDTO;
import com.devsuperior.dsmeta.dto.SaleSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.devsuperior.dsmeta.dto.SaleMinDTO;
import com.devsuperior.dsmeta.entities.Sale;
import com.devsuperior.dsmeta.repositories.SaleRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleService {

	@Autowired
	private SaleRepository repository;

	@Transactional(readOnly = true)
	public SaleMinDTO findById(Long id) {
		Optional<Sale> result = repository.findById(id);
		Sale entity = result.get();
		return new SaleMinDTO(entity);
	}

	@Transactional(readOnly = true)
	public Page<SaleReportDTO> findSalesReport(String minDate, String maxDate, String name, Pageable pageable) {

		LocalDate parsedMinDate = (minDate == null || minDate.isBlank()) ? null : LocalDate.parse(minDate);
		LocalDate parsedMaxDate = (maxDate == null || maxDate.isBlank()) ? null : LocalDate.parse(maxDate);

		if (name == null) { name = ""; }

		if (parsedMaxDate == null) { parsedMaxDate = LocalDate.ofInstant(Instant.now(), ZoneId.systemDefault()); }

		if (parsedMinDate == null) { parsedMinDate = parsedMaxDate.minusYears(1L); }

        return repository.findSalesReport(parsedMinDate, parsedMaxDate, name, pageable);
	}

	public List<SaleSummaryDTO> findSalesSummary(String minDate, String maxDate) {

		LocalDate parsedMinDate = (minDate == null || minDate.isBlank()) ? null : LocalDate.parse(minDate);
		LocalDate parsedMaxDate = (maxDate == null || maxDate.isBlank()) ? null : LocalDate.parse(maxDate);

		if (parsedMaxDate == null) { parsedMaxDate = LocalDate.ofInstant(Instant.now(), ZoneId.systemDefault()); }
		if (parsedMinDate == null) { parsedMinDate = parsedMaxDate.minusYears(1L); }

		return repository.findSalesSummary(parsedMinDate, parsedMaxDate);
	}
}
