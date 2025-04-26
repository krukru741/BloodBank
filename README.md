# Blood Bank Android Application

A modern Android application that connects blood donors with recipients, making the blood donation process more accessible and efficient through real-time communication and emergency response systems.

## Key Features

### 1. Emergency Response System
- **Priority-Based Requests**
  - Three-tier priority system (Normal, Urgent, Critical)
  - Location-based donor matching
  - Real-time status updates
  - Multi-channel notifications (Push, SMS, Email)
  - Emergency contact integration

### 2. User Management
- **Dual Registration System**
  - Donor registration with complete profile
  - Recipient registration with specific needs
  - Email verification system
  - Secure authentication
  - Role-based access control

### 3. Health & Eligibility Tracking
- **Comprehensive Health Monitoring**
  - Medical parameter tracking (Hemoglobin, Blood Pressure, Weight)
  - Eligibility status calculation
  - Donation interval tracking
  - Health history maintenance
  - Automatic deferral management

### 4. Achievement & Motivation System
- **Gamification Features**
  - Points-based reward system
  - Badge collection
  - Donor titles and ranks
  - Donation streaks
  - Community leaderboard

### 5. Real-time Communication
- **Multi-channel Notifications**
  - Priority-based alerts
  - Location-aware messaging
  - Emergency broadcast system
  - Status updates
  - Appointment reminders

## System Architecture

### Core Components
1. **Emergency Management**
   - Request creation and prioritization
   - Location-based matching
   - Real-time status tracking
   - Response management
   - Multi-channel notifications

2. **Health Monitoring**
   - Medical parameter tracking
   - Eligibility calculation
   - Donation scheduling
   - Health history management
   - Safety compliance

3. **Achievement System**
   - Points calculation
   - Badge unlocking
   - Title progression
   - Streak tracking
   - Community ranking

4. **Notification Engine**
   - Priority-based delivery
   - Multi-channel support
   - Real-time updates
   - Offline handling
   - Response tracking

## Technical Implementation

### Firebase Integration
```gradle
dependencies {
    // Firebase Core
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-database'
    implementation 'com.google.firebase:firebase-storage'
    implementation 'com.google.firebase:firebase-messaging'
    
    // Location Services
    implementation 'com.google.android.gms:play-services-location'
    
    // UI Components
    implementation 'androidx.appcompat:appcompat'
    implementation 'com.google.android.material:material'
    implementation 'androidx.constraintlayout:constraintlayout'
    implementation 'androidx.recyclerview:recyclerview'
    
    // Image Handling
    implementation 'com.github.bumptech.glide:glide'
    implementation 'de.hdodenhof:circleimageview'
    
    // Utilities
    implementation 'com.google.code.gson:gson'
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout'
}
```

### Database Structure
```
firebase/
├── users/
│   ├── donorId/
│   │   ├── profile
│   │   ├── health
│   │   └── achievements
├── emergency_requests/
│   ├── requestId/
│   │   ├── details
│   │   ├── status
│   │   └── responses
├── notifications/
│   ├── userId/
│   │   └── notificationId
└── donations/
    ├── donorId/
    │   └── donationId
```

### Security Implementation
- Firebase Authentication
- Role-based access control
- Data encryption
- Input validation
- Secure API endpoints
- Rate limiting
- Session management

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or newer
- JDK 11 or higher
- Android SDK 31 or higher
- Firebase account
- Google Maps API key

### Installation
1. Clone the repository
```bash
git clone [repository-url]
```

2. Configure Firebase
   - Create a new Firebase project
   - Add Android app to Firebase
   - Download `google-services.json`
   - Place in `app/` directory

3. Configure Google Maps
   - Get API key from Google Cloud Console
   - Add to `local.properties`:
   ```
   MAPS_API_KEY=your_api_key
   ```

4. Build and Run
   - Open project in Android Studio
   - Sync Gradle files
   - Run on device or emulator

## Project Structure
```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/bloodbank/
│   │   │       ├── Adapter/         # RecyclerView adapters
│   │   │       ├── Model/           # Data models
│   │   │       ├── Util/            # Utility classes
│   │   │       ├── Service/         # Background services
│   │   │       └── Activity/        # UI components
│   │   └── res/
│   │       ├── layout/              # UI layouts
│   │       ├── values/              # Resources
│   │       └── drawable/            # Images and icons
└── google-services.json
```

## Contributing
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support
For support, please:
- Check the [documentation](docs/)
- Open an issue
- Contact the maintainers

## Acknowledgments
- Firebase for backend services
- Google Maps for location services
- Material Design for UI components
- All contributors and testers 