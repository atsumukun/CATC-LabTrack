package ph.edu.catc.labtrack.equipment.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.edu.catc.labtrack.equipment.model.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    boolean existsByAssetCode(String assetCode);
    List<Equipment> findByLocationContainingIgnoreCaseOrderByAssetCode(String location);
}
