package com.devkor.ifive.nadab.domain.typereport.application;

import com.devkor.ifive.nadab.domain.typereport.core.repository.TypeReportRepository;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TypeReportQueryService {

    private final UserRepository userRepository;
    private final TypeReportRepository typeReportRepository;


}
