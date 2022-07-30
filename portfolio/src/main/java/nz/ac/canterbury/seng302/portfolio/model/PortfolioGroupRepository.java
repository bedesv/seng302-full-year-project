package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Simple repository which stores PortfolioGroups
 */
@Repository
public interface PortfolioGroupRepository extends CrudRepository<PortfolioGroup, Integer> {

    PortfolioGroup findByGroupId(int groupId);

}
