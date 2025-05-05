# Blood Bank Application

A comprehensive Android application developed as part of a thesis project, focusing on managing blood donations, emergency requests, and connecting donors with recipients.

## Project Overview

This application was developed as part of a thesis project to address the critical need for efficient blood donation management systems. The project demonstrates the implementation of modern mobile technologies in healthcare management, specifically focusing on blood donation processes.

## Features

- **User Authentication**
  - Secure login and registration
  - Email verification
  - User profile management
  - Role-based access (Donors & Recipients)

- **Donor Features**
  - Profile management
  - Donation history tracking
  - Health status monitoring
  - Achievement system
  - Donation scheduling
  - Emergency request notifications
  - Blood group compatibility information

- **Recipient Features**
  - Emergency blood request creation
  - Donor search and matching
  - Request status tracking
  - Hospital information management
  - Blood group compatibility checking

- **General Features**
  - Real-time notifications
  - Blood group compatibility information
  - Donation center locator
  - FAQ section
  - About Us information
  - Dark mode support
  - Multi-language support

## Technical Stack

- **Frontend**
  - Android (Java)
  - Material Design Components
  - RecyclerView for lists
  - Navigation Drawer
  - Custom UI components
  - ViewPager2 for onboarding
  - Bottom Navigation
  - CardView for content display

- **Backend**
  - Firebase Authentication
  - Firebase Realtime Database
  - Firebase Cloud Messaging

## Setup Instructions

1. **Prerequisites**
   - Android Studio (latest version)
   - Java Development Kit (JDK)
   - Android SDK
   - Firebase account

2. **Configuration**
   - Clone the repository
   - Open project in Android Studio
   - Add your `google-services.json` file to the app directory
   - Sync Gradle files
   - Build and run the application

3. **Firebase Setup**
   - Create a new Firebase project
   - Enable Authentication (Email/Password)
   - Set up Realtime Database
   - Configure Cloud Messaging
   - Update security rules

## Color Scheme

- Primary: #E53935 (Red)
- Primary Dark: #C62828
- Primary Light: #FFCDD2
- Accent: #FF5252
- Accent Dark: #D32F2F
- Background: #FFFFFF
- Text: #000000
- Secondary Text: #757575

## Building the Release APK

1. Open the project in Android Studio
2. Go to Build > Generate Signed Bundle / APK
3. Select APK
4. Choose your keystore file and enter credentials
5. Select release build type
6. Click Finish

The release APK will be generated at: `app/build/outputs/apk/release/app-release.apk`

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

For any queries or support, please contact the development team.

---

**Thesis Project Note**: This application was developed as part of an academic thesis project. While it demonstrates the implementation of a blood bank management system using modern mobile technologies, it should be noted that:

1. This is a research project and not a production-ready application
2. The system requires proper security audits and compliance checks before being used in a real-world healthcare setting
3. The implementation follows academic research methodologies and best practices
4. The project serves as a proof of concept for integrating mobile technologies in healthcare management systems
5. Further research and development would be required for deployment in a production environment

For academic inquiries or research collaboration, please contact the author. 