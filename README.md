# 🛒 Ecommerce Multi‑Vendor Platform

![Java](https://img.shields.io/badge/Java-SpringBoot-6DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-TypeScript-3178C6?logo=react&logoColor=white)
![Database](https://img.shields.io/badge/DB-MySQL-4479A1?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?logo=kubernetes&logoColor=white)
![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF?logo=github-actions&logoColor=white)
![Status](https://img.shields.io/badge/Status-In_Progress-yellow)

An advanced **multi‑vendor e‑commerce** web application with a **Spring Boot** backend and a **React + TypeScript** frontend.  
Supports **customers, sellers, and admins** with authentication, product/catalog management, checkout with **Razorpay/Stripe**, analytics, and more. 

Built with a strong focus on **DevSecOps, Scalability, and Cloud-Native** practices.

---

## 📁 Monorepo Layout


Ecommerce-react-spring-boot/
├─ Ecomm-react-tsx-frontend/     # React + TypeScript app (Redux Toolkit, MUI, Tailwind)
├─ Ecomm-springboot-backend/      # Spring Boot REST API (JWT, Spring Security, MySQL)
├─ .github/workflows/             # GitHub Actions CI/CD Pipelines
└─ k8s-manifests/                 # Helm Charts & Kubernetes YAML files



---

## 🧱 Tech & DevOps Stack

### Backend

* Spring Boot, Spring Security, JWT, Java Mail Sender
* MySQL (JPA/Hibernate)

### Frontend

* React (TypeScript), Redux Toolkit, React Router DOM, Axios
* Tailwind CSS, MUI, Formik + Yup

### DevOps & Cloud Infrastructure 🚀

* **CI/CD:** GitHub Actions
* **Code Quality & Security:** SonarCloud, OWASP Dependency Check, Trivy (Container Scanning)
* **Containerization:** Docker & Docker Hub
* **Orchestration:** Kubernetes (K8s), Helm Charts
* **GitOps:** ArgoCD (Continuous Deployment)
* **Observability:** Prometheus & Grafana (Planned)

### Payments

* **Razorpay** (India) & **Stripe** (International)

---

## ⚙️ Architecture & CI/CD Flow


graph TD
    A[Developer Pushes Code] --> B[GitHub Actions CI]
    B --> C{Tests & Code Quality}
    C -->|SonarCloud + OWASP| D[Docker Image Build]
    D --> E[Trivy Vulnerability Scan]
    E -->|Pass| F[Push to Docker Hub]
    F --> G[Update Helm Chart/Manifests]
    G --> H[ArgoCD Detects Changes]
    H --> I((Kubernetes Cluster))
    I --> J[Deploy Frontend Pods]
    I --> K[Deploy Backend Pods]



---

## ✨ Core Features

### 👤 Customer

* Chatbot for queries (order history, cart, product details)
* Browse products with **filter, sort, pagination**
* Cart & Checkout: **coupons**, **addresses**, **Razorpay/Stripe**
* Wishlist, Reviews & ratings

### 🛍️ Seller

* Dashboard with **earnings graphs** (today / last 7 days / last 12 months)
* Reports: sales, earnings, refunds, cancellations
* Product & Order management

### 🛠️ Admin

* Seller approval/suspension
* Coupon & Deal/promo management

---

## 🗂️ Core Entities (ER Overview)


erDiagram
  User ||--o{ Address : has
  User ||--o{ Order : places
  User ||--o{ Review : writes
  User ||--o{ Transaction : performs
  User ||--|| Wishlist : "has 1"
  User ||--|| Cart : "has 1"

  Cart ||--o{ CartItem : contains
  CartItem }o--|| Product : refers

  Product }o--|| Category : belongs_to
  Product ||--o{ Review : receives
  Product }o--|| Seller : offered_by

  Order ||--o{ OrderItem : contains
  OrderItem }o--|| Product : refers
  Order }o--|| Address : ships_to
  PaymentOrder ||--o{ Order : includes

  Coupon }o--o{ User : used_by

  Seller ||--o{ Product : sells
  Seller ||--o{ Transaction : participates_in
  Seller ||--|| SellerReport : "has 1"



---

## 🚀 Getting Started (Local Development)

### 1) Backend (Spring Boot)

```bash
cd Ecomm-springboot-backend
./mvnw spring-boot:run    # Linux/macOS

```

*(Require `application.properties` with MySQL, JWT, Mail, and Payment credentials)*

### 2) Frontend (React + TS)

```bash
cd Ecomm-react-tsx-frontend
npm install
npm run dev   # http://localhost:5173

```

*(Require `.env` with API Base URL and Payment Public Keys)*

---

## ☸️ Running on Kubernetes (Local Cluster)

Ensure you have `Docker`, `Minikube`/`Kind`, and `Helm` installed.

```bash
# Apply ConfigMaps and Secrets
kubectl apply -f k8s-manifests/secrets.yaml

# Install Backend and Frontend via Helm
helm install ecomm-backend ./k8s-manifests/backend-chart
helm install ecomm-frontend ./k8s-manifests/frontend-chart

# Verify Pods are running
kubectl get pods -n default

```

---

## 🛣️ Roadmap

* [x] Full Stack App Development (Spring Boot + React)
* [x] Role-Based Access Control (Admin, Seller, Customer)
* [x] Automated CI Pipeline (GitHub Actions)
* [x] Code Quality & Security Scanning (SonarCloud, OWASP)
* [ ] Containerization & Kubernetes Deployment (In Progress)
* [ ] Setup ArgoCD for GitOps automated deployments
* [ ] Implement Monitoring with Prometheus & Grafana
* [ ] Infrastructure setup via Terraform

---

## 📸 Screenshots

*(Add your screenshots here: `assets/images/homepage.png` etc.)*

---

## 🔐 Security Notes

* Secrets (DB credentials, API Keys) are injected via **GitHub Secrets** during CI/CD.
* Container images are automatically scanned for CVEs using **Trivy**.
* Strict **RBAC** implemented at the API level using Spring Security.

---

## 📄 License

MIT — see `LICENSE` for details.

