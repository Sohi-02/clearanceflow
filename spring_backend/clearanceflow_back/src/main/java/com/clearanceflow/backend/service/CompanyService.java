package com.clearanceflow.backend.service;

import com.clearanceflow.backend.dto.CompanyRequestDTO;
import com.clearanceflow.backend.dto.CompanyResponseDTO;
import com.clearanceflow.backend.entity.Company;
import com.clearanceflow.backend.exception.CompanyNotFoundException;
import com.clearanceflow.backend.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import com.clearanceflow.backend.entity.User;
import com.clearanceflow.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyService(CompanyRepository companyRepository,
                          UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    // CREATE COMPANY
    @Transactional
    public CompanyResponseDTO createCompany(CompanyRequestDTO request) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName(); //Spring Security extracts the authenticated user's identity from the verified JWT.
        // We then retrieve that user from PostgreSQL.

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));

        if (user.getCompany() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You are already associated with a company"
            );
        }

        Company company = new Company();

        updateCompanyFields(company, request);

        Company savedCompany = companyRepository.save(company);

        user.setCompany(savedCompany); //This associates the registered applicant with the newly created company.
        userRepository.save(user);

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

        checkCompanyOwnership(id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException(id));

        return mapToResponseDTO(company);
    }

    // UPDATE COMPANY
    public CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO request) {

        checkCompanyOwnership(id);
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException(id));

        updateCompanyFields(company, request);

        Company updatedCompany = companyRepository.save(company);

        return mapToResponseDTO(updatedCompany);
    }

    // DELETE COMPANY
    public void deleteCompany(Long id) {

        checkCompanyOwnership(id);
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

    private void checkCompanyOwnership(Long companyId) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));

        if (user.getCompany() == null ||
                !user.getCompany().getId().equals(companyId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to access this company"
            );
        }
    }
}