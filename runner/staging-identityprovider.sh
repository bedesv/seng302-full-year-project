fuser -k 9500/tcp || true
export $(cat staging-identityprovider/.env | xargs -d '\n')
SPRING_PROFILES_ACTIVE=deploy java -jar staging-identityprovider/libs/identityprovider-0.0.1-SNAPSHOT.jar \
    --spring.mvc.servlet.path=/test/identity \
    --spring.application.name=identity-provider \
    --grpc.server.port=9500