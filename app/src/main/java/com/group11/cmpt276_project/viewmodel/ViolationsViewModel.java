package com.group11.cmpt276_project.viewmodel;

import com.group11.cmpt276_project.service.model.Violation;
import com.group11.cmpt276_project.service.repository.ViolationRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A singleton class that contains a map of all violations given. Violations are accessed through
 * respective IDs
 */
public class ViolationsViewModel {
    private Map<String, Violation> violations;
    private ViolationRepository violationRepository;

    private ViolationsViewModel() {}

    private static class ViolationsViewModelHolder {
        private static ViolationsViewModel INSTANCE = new ViolationsViewModel();
    }

    public static ViolationsViewModel getInstance() {
        return ViolationsViewModelHolder.INSTANCE;
    }

    public void init(ViolationRepository violationRepository) {
        if (this.violationRepository == null) {
            this.violationRepository = violationRepository;

            try {
                this.violations = this.violationRepository.get();
            }
            catch (IOException e) {
                this.violations = new HashMap<>();
            }
        }
    }

    public Violation get(String id) {
        return violations.get(id);
    }
}
