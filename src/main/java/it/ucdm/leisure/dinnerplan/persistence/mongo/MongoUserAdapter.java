package it.ucdm.leisure.dinnerplan.persistence.mongo;

import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.persistence.UserRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.mongo.document.UserDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("mongo")
public class MongoUserAdapter implements UserRepositoryPort {

    private final MongoUserRepository mongoUserRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public MongoUserAdapter(MongoUserRepository mongoUserRepository,
            SequenceGeneratorService sequenceGeneratorService) {
        this.mongoUserRepository = mongoUserRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public User save(User user) {
        UserDocument doc = UserDocument.fromDomain(user);
        if (doc.getId() == null) {
            doc.setId(sequenceGeneratorService.generateSequence(UserDocument.class.getSimpleName()));
        }
        return mongoUserRepository.save(doc).toDomain();
    }

    @Override
    public Optional<User> findById(Long id) {
        return mongoUserRepository.findById(id).map(UserDocument::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return mongoUserRepository.findByUsername(username).map(UserDocument::toDomain);
    }

    @Override
    public List<User> findAll() {
        return mongoUserRepository.findAll().stream()
                .map(UserDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        mongoUserRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return mongoUserRepository.findByUsername(username).isPresent();
    }
}
