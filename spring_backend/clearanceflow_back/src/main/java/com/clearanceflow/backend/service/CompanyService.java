package com.clearanceflow.backend.service;

import com.clearanceflow.backend.dto.CompanyRequestDTO;
import com.clearanceflow.backend.dto.CompanyResponseDTO;
import com.clearanceflow.backend.entity.Company;
import com.clearanceflow.backend.exception.CompanyNotFoundException;
import com.clearanceflow.backend.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    // CONSTRUCTOR
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    // CREATE COMPANY
    public CompanyResponseDTO createCompany(CompanyRequestDTO request) {

        Company company = new Company();

        updateCompanyFields(company, request);

        Company savedCompany = companyRepository.save(company);

        return mapToResponseDTO(savedCompany);
    }

    // GET ALL COMPANIES
    public List<CompanyResponseDTO> getAllCompanies() {

        return companyRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // GET COMPANY BY ID
    public CompanyResponseDTO getCompanyById(Long id) {

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException(id));

        return mapToResponseDTO(company);
    }

    // UPDATE COMPANY
    public CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO request) {

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException(id));

        updateCompanyFields(company, request);

        Company updatedCompany = companyRepository.save(company);

        return mapToResponseDTO(updatedCompany);
    }

    // DELETE COMPANY
    public void deleteCompany(Long id) {

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException(id));

        companyRepository.delete(company);
    }

    // CONVERT ENTITY TO RESPONSE DTO
    private CompanyResponseDTO mapToResponseDTO(Company company) {

        CompanyResponseDTO response = new CompanyResponseDTO();

        response.setId(company.getId());
        response.setName(company.getName());
        response.setIndustry(company.getIndustry());
        response.setState(company.getState());
        response.setCompanySize(company.getCompanySize());
        response.setProjectType(company.getProjectType());

        return response;
    }

    // COPY REQUEST FIELDS INTO ENTITY
    private void updateCompanyFields(Company company, CompanyRequestDTO request) {

        company.setName(request.getName());
        company.setIndustry(request.getIndustry());
        company.setState(request.getState());
        company.setCompanySize(request.getCompanySize());
        company.setProjectType(request.getProjectType());
    }
}