# File Damage Analyzer

A web service for analyzing damaged files in directories.  
The service compares files between two directories, identifies differences, and provides a web interface and REST API for detailed analysis.

---

## Features

- **Web Interface** — User-friendly Vue.js frontend for configuring and running analysis  
- **REST API** — Full API for programmatic access and integration  
- **File Comparison** — Byte-by-byte comparison of files to detect exact damage locations  
- **Real-time Analysis** — Asynchronous processing with progress tracking  
- **Detailed Reports** — Shows exact byte offsets and differences for damaged files  

---

## Technology Stack

| Component  | Technology |
|-------------|-------------|
| **Backend** | Java 21, Spring Boot 3.1.5 |
| **Frontend** | Vue.js 3, HTML5, CSS3 |
| **Build Tool** | Maven |
| **Packaging** | RPM for ALT Linux |

---

## Project Structure

file-damage-analyzer/
├── src/
│ ├── main/
│ │ ├── java/org/example/
│ │ │ ├── Application.java
│ │ │ ├── controller/
│ │ │ ├── service/
│ │ │ └── dto/
│ │ └── resources/
│ │ ├── static/
│ │ │ ├── css/
│ │ │ ├── js/
│ │ │ └── index.html
│ │ └── application.properties
├── rpm/
│ ├── file-damage-analyzer.spec
│ └── file-damage-analyzer.service
├── .gear/
│ └── rules
├── pom.xml
├── application.properties
├── LICENSE
└── README.md


---

## 1. Clone and Build
git clone <repository-url>
cd file-damage-analyzer

Build the application
mvn clean package

---

## 2. Run Locally

### Option 1: Run with JAR

java -jar target/file-damage-analyzer.jar

### Option 2: Run with Maven

mvn spring-boot:run


The application will be available at:  
👉 [http://localhost:8080/](http://localhost:8080/)

---

## API Endpoints

| Method | Endpoint               | Description                     |
|---------|------------------------|---------------------------------|
| POST    | `/api/analyze`         | Start analysis of directories   |
| GET     | `/api/results/{taskId}`| Get analysis results            |
| GET     | `/api/health`          | Health check                    |
| GET     | `/api/tasks`           | List active tasks               |

---

## 3. Building RPM Package

### Prerequisites (ALT Linux)

sudo apt-get update
sudo apt-get install -y gear hasher java-21-openjdk-devel maven rpm-build



### Build Process

#### 1. Build the JAR file

mvn clean package

#### 2. Prepare files for RPM build

cp target/file-damage-analyzer.jar .
cp rpm/file-damage-analyzer.service .

#### 3. Create source archive

mkdir -p file-damage-analyzer-1.0.0
cp -r src pom.xml application.properties README.md rpm LICENSE file-damage-analyzer-1.0.0/
tar czf file-damage-analyzer-1.0.0.tar.gz file-damage-analyzer-1.0.0/
rm -rf file-damage-analyzer-1.0.0

#### 4. Create SRPM package

gear-rpm

#### 5. Build RPM package with hasher

hasher-make

---

## 4. Installation from RPM

### Install the RPM Package

sudo rpm -ivh x86_64/file-damage-analyzer-1.0.0-alt1.x86_64.rpm

### Or upgrade existing installation

sudo rpm -Uvh x86_64/file-damage-analyzer-1.0.0-alt1.x86_64.rpm

---

## 5. Service Management

Start the service
sudo systemctl start file-damage-analyzer

Check service status
sudo systemctl status file-damage-analyzer

Enable autostart on boot
sudo systemctl enable file-damage-analyzer

Stop the service
sudo systemctl stop file-damage-analyzer

View logs
sudo journalctl -u file-damage-analyzer -f

---

## 6. Directory Structure After Installation

/etc/file-damage-analyzer/
└── application.properties # Configuration

/usr/share/file-damage-analyzer/
└── file-damage-analyzer.jar # Application JAR

/usr/bin/
└── file-damage-analyzer # Launch script

/usr/lib/systemd/system/
└── file-damage-analyzer.service # Systemd service

/var/log/file-damage-analyzer/ # Log directory
/var/lib/file-damage-analyzer/ # Data directory

---

## 7. Test with Sample Directories

Create test directories
mkdir -p /tmp/test/original
mkdir -p /tmp/test/damaged

Create test files
echo "Original content" > /tmp/test/original/file1.txt
echo "Damaged content" > /tmp/test/damaged/file1.txt
echo "Same content" > /tmp/test/original/file2.txt
echo "Same content" > /tmp/test/damaged/file2.txt

Now open the web interface or use the API to analyze:

- **Original directory:** `/tmp/test/original`  
- **Damaged directory:** `/tmp/test/damaged`

---

## 8. Health Check

curl http://localhost:8080/api/health
Expected response: {"status":"OK","service":"File Damage Analyzer"}

