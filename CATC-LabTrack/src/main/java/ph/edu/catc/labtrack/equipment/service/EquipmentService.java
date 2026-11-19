package ph.edu.catc.labtrack.equipment.service;

import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ph.edu.catc.labtrack.equipment.dto.*;
import ph.edu.catc.labtrack.equipment.model.Equipment;
import ph.edu.catc.labtrack.equipment.repository.EquipmentRepository;

@Service
public class EquipmentService {
    private final EquipmentRepository repository;

    public EquipmentService(EquipmentRepository repository) { this.repository = repository; }

    public List<EquipmentResponse> list(String location) {
        return repository.findByLocationContainingIgnoreCaseOrderByAssetCode(location.strip())
            .stream().map(EquipmentResponse::from).toList();
    }

    public Optional<EquipmentResponse> find(long id) {
        return repository.findById(id).map(EquipmentResponse::from);
    }

    public EquipmentResponse create(EquipmentRequest input) {
        String code = input.assetCode().toUpperCase(Locale.ROOT);
        if (repository.existsByAssetCode(code)) throw new DuplicateCodeException(code);
        var item = new Equipment(code, input.name().strip(), input.location().strip(), input.status());
        try {
            return EquipmentResponse.from(repository.saveAndFlush(item));
        } catch (DataIntegrityViolationException ex) {
            if (repository.existsByAssetCode(code)) throw new DuplicateCodeException(code);
            throw ex;
        }
    }
}
