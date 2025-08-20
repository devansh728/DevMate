# DevMate Platform 🚀

A real-time proximity-based developer collaboration platform that connects developers based on location, skills, and collaboration opportunities. Built with Spring Boot microservices, featuring AI-powered skill matching, live code collaboration, and comprehensive monitoring.

## 🌟 Features

### 🔗 Real-time Collaboration
- **Proximity-based matching**: Find developers near you using PostGIS geo-queries
- **Live code collaboration**: Real-time code editing similar to VS Code LiveShare
- **WebSocket chat**: Instant messaging with typing indicators and presence
- **AI-powered ice-breakers**: Gemini AI generates conversation starters

### 🛠️ Technical Stack
- **Backend**: Spring Boot 3.2, Java 17
- **Databases**: PostgreSQL with PostGIS for location data
- **Caching**: Redis for session management and real-time data
- **AI Integration**: Google Gemini API for skill matching and conversations
- **Real-time Communication**: WebSockets with STOMP protocol
- **Monitoring**: Prometheus, Grafana, ELK Stack
- **Infrastructure**: AWS EKS, Terraform, Docker, Kubernetes

### 📊 Observability & Chaos Engineering
- **Comprehensive monitoring** with Prometheus and Grafana dashboards
- **Distributed tracing** with Zipkin
- **Centralized logging** with ELK Stack
- **Chaos engineering** API for testing system resilience
- **Health checks** and metrics for all services

## 🏗️ Architecture

### Microservices
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   User Service  │    │ Location Service│    │  Chat Service   │
│   Port: 8081    │    │   Port: 8082    │    │   Port: 8083    │
└─────────────────┘    └─────────────────┘    └─────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│Collaboration Svc│    │   AI Service    │    │  Chaos Service  │
│   Port: 8085    │    │   Port: 8084    │    │   Port: 8086    │
└─────────────────┘    └─────────────────┘    └─────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Gateway Service │    │ Config Service  │    │Discovery Service│
│   Port: 8080    │    │   Port: 8888    │    │   Port: 8761    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Infrastructure Components
- **API Gateway**: Routes requests and handles cross-cutting concerns
- **Service Discovery**: Eureka for service registration and discovery
- **Configuration**: Centralized config management
- **Load Balancing**: AWS Application Load Balancer
- **Auto Scaling**: Kubernetes HPA and Cluster Autoscaler
- **CI/CD**: Jenkins with Kubernetes agents

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- kubectl (for Kubernetes deployment)
- Terraform (for AWS infrastructure)
- AWS CLI configured

### Local Development

1. **Clone the repository**
```bash
git clone https://github.com/your-org/devmate-platform.git
cd devmate-platform
```

2. **Set up environment variables**
```bash
cp .env.example .env
# Edit .env with your configuration
export GEMINI_API_KEY="your-gemini-api-key"
```

3. **Start with Docker Compose**
```bash
# Build all services
chmod +x docker/build-services.sh
./docker/build-services.sh

# Start the platform
docker-compose up -d

# View logs
docker-compose logs -f
```

4. **Access the services**
- API Gateway: http://localhost:8080
- Eureka Dashboard: http://localhost:8761
- Grafana: http://localhost:3000 (admin/admin123)
- Prometheus: http://localhost:9090
- Kibana: http://localhost:5601

### Building Individual Services

```bash
# Build all services
mvn clean package

# Build specific service
mvn clean package -pl user-service -am

# Run specific service
cd user-service
mvn spring-boot:run
```

## ☁️ AWS Deployment

### Infrastructure Setup

1. **Configure Terraform**
```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your values
```

2. **Deploy Infrastructure**
```bash
# Initialize Terraform
terraform init

# Plan deployment
terraform plan

# Deploy infrastructure
terraform apply
```

3. **Configure kubectl**
```bash
aws eks update-kubeconfig --region us-west-2 --name devmate-dev-eks
```

### Application Deployment

1. **Deploy to Kubernetes**
```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/services/
kubectl apply -f k8s/gateway/
kubectl apply -f k8s/monitoring/

# Verify deployment
kubectl get pods -n devmate
kubectl get services -n devmate
```

2. **Set up CI/CD**
```bash
# Deploy Jenkins
kubectl apply -f jenkins/jenkins-k8s.yaml

# Get Jenkins initial password
kubectl exec -n jenkins deployment/jenkins -- cat /var/jenkins_home/secrets/initialAdminPassword
```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `GEMINI_API_KEY` | Google Gemini API key | Required |
| `DB_HOST` | Database host | localhost |
| `DB_USERNAME` | Database username | devmate |
| `DB_PASSWORD` | Database password | devmate123 |
| `REDIS_HOST` | Redis host | localhost |
| `EUREKA_URL` | Eureka server URL | http://localhost:8761/eureka |

### Database Configuration

Each service uses its own PostgreSQL database:
- **user-service**: `devmate_users`
- **location-service**: `devmate_locations` (with PostGIS)
- **chat-service**: `devmate_chat`
- **collaboration-service**: `devmate_collaboration`

## 📡 API Documentation

### User Service (Port 8081)
- `GET /api/users` - Get all users
- `POST /api/users` - Create user
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/search?q={term}` - Search users
- `GET /api/users/skills?skills={skills}` - Find users by skills

### Location Service (Port 8082)
- `POST /api/locations` - Update user location
- `GET /api/locations/nearby` - Find nearby users
- `GET /api/locations/closest` - Get closest users
- `WebSocket: /ws/location` - Real-time location updates

### Chat Service (Port 8083)
- `POST /api/chat/rooms` - Create chat room
- `GET /api/chat/rooms/user/{userId}` - Get user's chat rooms
- `POST /api/chat/messages` - Send message
- `WebSocket: /ws/chat` - Real-time messaging

### AI Service (Port 8084)
- `POST /api/ai/skill-matching` - Find skill matches
- `POST /api/ai/ice-breakers` - Generate conversation starters

### Collaboration Service (Port 8085)
- `POST /api/collaboration/sessions` - Create collaboration session
- `GET /api/collaboration/sessions/{id}` - Get session details
- `POST /api/collaboration/documents` - Create code document
- `WebSocket: /ws/collaboration` - Real-time code collaboration

### Chaos Service (Port 8086)
- `POST /api/chaos/experiments` - Start chaos experiment
- `GET /api/chaos/experiments/active` - Get active experiments
- `POST /api/chaos/demo/simulate-load` - Demo load simulation

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn test -Dtest=**/*IntegrationTest
```

### Chaos Engineering
```bash
# Simulate high CPU load
curl -X POST http://localhost:8086/api/chaos/demo/simulate-load?durationSeconds=30

# Simulate memory leak
curl -X POST http://localhost:8086/api/chaos/demo/simulate-memory-leak?durationSeconds=60

# View chaos experiments
curl http://localhost:8086/api/chaos/experiments/active
```

## 📊 Monitoring

### Grafana Dashboards
- **DevMate Overview**: System health and key metrics
- **Service Performance**: Individual service metrics
- **Infrastructure**: Kubernetes cluster metrics
- **Business Metrics**: User activity and collaboration stats

### Key Metrics
- Request rate and response times
- Error rates and success rates
- Database connection pools
- Memory and CPU usage
- Active users and sessions
- WebSocket connections

### Alerts
- High error rates (>5%)
- High response times (>2s)
- Database connection issues
- Memory usage >80%
- Pod restart events

## 🔒 Security

### Authentication & Authorization
- JWT tokens for API authentication
- Role-based access control (RBAC)
- Service-to-service authentication
- Rate limiting and throttling

### Data Protection
- Encryption at rest and in transit
- Secrets management with AWS Secrets Manager
- Network policies in Kubernetes
- Regular security scans with Trivy

### Privacy
- Location data anonymization
- GDPR compliance measures
- Data retention policies
- User consent management

## 🎯 Demo Features

### Quick Wins for Demo Impact

1. **Real-time Location Tracking**
   ```bash
   # Update location
   curl -X POST http://localhost:8082/api/locations \
     -H "Content-Type: application/json" \
     -d '{"userId":"user1","latitude":37.7749,"longitude":-122.4194}'
   
   # Find nearby users
   curl "http://localhost:8082/api/locations/nearby?userId=user1&latitude=37.7749&longitude=-122.4194&radiusKm=5"
   ```

2. **AI-Powered Skill Matching**
   ```bash
   curl -X POST http://localhost:8084/api/ai/skill-matching \
     -H "Content-Type: application/json" \
     -d '{"userId":"user1","userSkills":["Java","React"],"nearbyUsers":[...]}'
   ```

3. **Chaos Engineering Demo**
   ```bash
   # Start load simulation
   curl -X POST http://localhost:8086/api/chaos/demo/simulate-load?durationSeconds=30
   
   # Watch metrics in Grafana
   open http://localhost:3000
   ```

4. **Live Code Collaboration**
   - Open collaboration session
   - Multiple users join session
   - Real-time code editing
   - See cursors and selections

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Spring Boot best practices
- Write comprehensive tests
- Update documentation
- Use conventional commits
- Ensure all checks pass

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Google for the Gemini AI API
- PostGIS for spatial database capabilities
- The open-source community for amazing tools

## 📞 Support

- 📧 Email: support@devmate.dev
- 💬 Slack: [#devmate-support](https://your-org.slack.com/channels/devmate-support)
- 📚 Documentation: [docs.devmate.dev](https://docs.devmate.dev)
- 🐛 Issues: [GitHub Issues](https://github.com/your-org/devmate-platform/issues)

---

**Built with ❤️ by the DevMate Team**

Ready to connect developers worldwide! 🌍👨‍💻👩‍💻
