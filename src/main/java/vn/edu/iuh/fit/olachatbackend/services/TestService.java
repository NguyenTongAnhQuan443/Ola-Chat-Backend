package vn.edu.iuh.fit.olachatbackend.services;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.dtos.TestDto;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;

@Service
public class TestService {

    public TestDto getTestDto() {
        throw new BadRequestException("Test exception");
    }
}
