package ph.edu.catc.labtrack.equipment.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ph.edu.catc.labtrack.equipment.model.Equipment;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    boolean existsByAssetCodeIgnoreCase(String assetCode);

    List<Equipment> findByLocationContainingIgnoreCaseOrderByAssetCode(String location);

    /** Locks the row so two people can't reserve the last unit at the same moment. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Equipment e where e.id = :id")
    Optional<Equipment> findForUpdate(@Param("id") Long id);
}