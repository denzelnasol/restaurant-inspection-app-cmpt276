package com.group11.cmpt276_project.service.repository.impl.db;

import androidx.lifecycle.LiveData;

import com.group11.cmpt276_project.exception.RepositoryReadError;
import com.group11.cmpt276_project.exception.RepositoryWriteError;
import com.group11.cmpt276_project.service.dao.ViolationDao;
import com.group11.cmpt276_project.service.db.RestaurantDatabase;
import com.group11.cmpt276_project.service.model.Violation;
import com.group11.cmpt276_project.service.repository.IViolationRepository;
import com.group11.cmpt276_project.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Room database to handle violations
 */
public class RoomViolationRepository implements IViolationRepository {

    private ViolationDao violationDao;

    public RoomViolationRepository(ViolationDao violationDao) {
        this.violationDao = violationDao;
    }

    @Override
    public LiveData<List<Violation>> getViolations() throws RepositoryReadError {
        return this.violationDao.getAllViolations(Constants.SUPPORTED_LANGUAGES.getOrDefault(Locale.getDefault().getISO3Language(), Constants.ENG));
    }

    @Override
    public void saveViolations(List<Violation> violations) throws RepositoryWriteError {
        this.violationDao.insertOrUpdate(violations);
    }
}
