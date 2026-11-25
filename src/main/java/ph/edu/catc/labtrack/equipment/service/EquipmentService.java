package ph.edu.catc.labtrack.equipment.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ph.edu.catc.labtrack.equipment.dto.EquipmentRequest;
import ph.edu.catc.labtrack.equipment.dto.EquipmentResponse;
import ph.edu.catc.labtrack.equipment.model.Equipment;
import ph.edu.catc.labtrack.equipment.model.Status;
import ph.edu.catc.labtrack.equipment.repository.EquipmentRepository;

import java.util.Locale;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class EquipmentService {
    private final EquipmentRepository repository;

    public EquipmentService(EquipmentRepository repository) { this.repository = repository; }

    public List<EquipmentResponse> list(String location) {
        return list(location, null);
    }

    /** Lists equipment, optionally narrowed to a location text and/or a status. */
    public List<EquipmentResponse> list(String location, Status status) {
        return repository.findByLocationContainingIgnoreCaseOrderByAssetCode(location.strip())
                .stream()
                .filter(item -> status == null || item.getStatus() == status)
                .map(EquipmentResponse::from)
                .toList();
    }

    public Optional<EquipmentResponse> find(long id) {
        return repository.findById(id).map(EquipmentResponse::from);
    }

    public EquipmentResponse get(long id) {
        return find(id).orElseThrow(() -> new EquipmentNotFoundException(id));
    }

    @Transactional
    public EquipmentResponse update(long id, EquipmentRequest input) {
        var item = repository.findForUpdate(id).orElseThrow(() -> new EquipmentNotFoundException(id));
        String code = input.assetCode().toUpperCase(Locale.ROOT);
        if (!item.getAssetCode().equals(code) && repository.existsByAssetCodeIgnoreCase(code)) {
            throw new DuplicateCodeException(code);
        }
        item.update(code, input.name().strip(), input.location().strip(), input.status(), input.totalUnits(), input.description(), input.imageUrl());
        repository.flush();
        return EquipmentResponse.from(item);
    }

    @Transactional
    public void delete(long id) {
        var item = repository.findForUpdate(id).orElseThrow(() -> new EquipmentNotFoundException(id));
        repository.delete(item);
    }

    public EquipmentResponse create(EquipmentRequest input) {
        String code = input.assetCode().toUpperCase(Locale.ROOT);
        if (repository.existsByAssetCodeIgnoreCase(code)) throw new DuplicateCodeException(code);
        int units = input.totalUnits() == null ? 1 : input.totalUnits();
        var item = new Equipment(code, input.name().strip(), clean(input.description()), clean(input.imageUrl()),
                input.location().strip(), input.status(), units);
        try {
            return EquipmentResponse.from(repository.saveAndFlush(item));
        } catch (DataIntegrityViolationException ex) {
            if (repository.existsByAssetCodeIgnoreCase(code)) throw new DuplicateCodeException(code);
            throw ex;
        }
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}