package app.company.service;

import app.company.model.Company;
import app.company.repository.CompanyRepository;
import app.exceptions.CompanyNotFound;
import app.product.model.Product;
import app.product.model.ProductCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public int allCompaniesCount() {
        return companyRepository.findAll().size();
    }

    public void saveCompany(Company company) {
        companyRepository.save(company);
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company getCompanyById(UUID brandId) {
        return companyRepository.findById(brandId).orElseThrow(() -> new CompanyNotFound("Company with id [%s] not found]".formatted(brandId)));
    }
}
