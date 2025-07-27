# 🏆 BetPredict Pro - Android Betting Prediction App

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)

A comprehensive Android application for sports betting predictions featuring free and premium content, expert analysis, and subscription management.

## 📱 Screenshots

| Home Screen | Predictions | Premium | Results |
|-------------|-------------|---------|---------|
| ![Home](screenshots/home.png) | ![Predictions](screenshots/predictions.png) | ![Premium](screenshots/premium.png) | ![Results](screenshots/results.png) |

## ✨ Features

### 🆓 Free Features
- **Daily Free Predictions**: Access to daily betting predictions with match details
- **Prediction of the Day (POTD)**: Featured daily prediction with expert analysis
- **Past Results**: Historical tracking of prediction performance
- **Success Statistics**: View win/loss rates and performance charts

### 💎 Premium Features
- **Exclusive Predictions**: High-confidence premium predictions
- **Expert Analysis**: Detailed match analysis and reasoning
- **Early Access**: Get predictions 2-4 hours before free users
- **Multiple Daily Tips**: Access to multiple premium predictions per day

### 👤 User Management
- **Registration & Authentication**: Secure user registration with email/phone verification
- **Premium Subscriptions**: Monthly subscription with mobile money payment
- **Profile Management**: Customize preferences and view subscription status

### 🔧 Expert Panel
- **Content Management**: Add, edit, and manage predictions
- **Payment Verification**: Verify user payments and activate subscriptions
- **Results Tracking**: Mark prediction outcomes and maintain accuracy

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM with Repository Pattern
- **UI Framework**: Jetpack Compose with Material Design 3
- **Database**: Firebase Firestore
- **Authentication**: Firebase Auth
- **Notifications**: Firebase Cloud Messaging
- **Storage**: Firebase Storage
- **Minimum SDK**: Android 7.0 (API level 24)

## 🏗️ Architecture

```
app/
├── data/
│   ├── repository/
│   ├── remote/
│   └── local/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
├── presentation/
│   ├── ui/
│   ├── viewmodel/
│   └── theme/
└── di/
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or higher
- Android SDK 24+
- Firebase project setup

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/betpredict-pro.git
   cd betpredict-pro
   ```

2. **Firebase Setup**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Add your Android app to the project
   - Download `google-services.json` and place it in the `app/` directory
   - Enable Authentication, Firestore, and Cloud Messaging

3. **Configure API Keys**
   ```kotlin
   // Create local.properties file in root directory
   MAPS_API_KEY=your_maps_api_key_here
   PAYMENT_API_KEY=your_payment_api_key_here
   ```

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

## 📋 Configuration

### Firebase Collections Structure

**Users Collection:**
```json
{
  "userId": "string",
  "email": "string",
  "phoneNumber": "string",
  "isPremium": "boolean",
  "subscriptionExpiry": "timestamp",
  "favoritesSports": ["array"],
  "registrationDate": "timestamp"
}
```

**Predictions Collection:**
```json
{
  "predictionId": "string",
  "expertId": "string",
  "matchDetails": {
    "homeTeam": "string",
    "awayTeam": "string",
    "league": "string",
    "matchDate": "timestamp"
  },
  "predictionType": "string",
  "confidenceLevel": "number",
  "category": "free|premium",
  "isPOTD": "boolean",
  "resultStatus": "pending|won|lost"
}
```

## 🎯 Usage

### For End Users
1. **Register**: Create account with email/phone verification
2. **Browse**: View free predictions and POTD
3. **Subscribe**: Purchase premium access via mobile money
4. **Track**: Monitor prediction results and success rates

### For Experts/Admins
1. **Login**: Access expert panel with admin credentials
2. **Add Predictions**: Create new predictions with analysis
3. **Verify Payments**: Confirm user payments and activate subscriptions
4. **Track Results**: Update prediction outcomes

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🐛 Bug Reports & Feature Requests

Please use the [GitHub Issues](https://github.com/yourusername/betpredict-pro/issues) page to report bugs or request features.

## 📞 Support

- **Email**: support@betpredict.com
- **Documentation**: [Wiki](https://github.com/yourusername/betpredict-pro/wiki)
- **FAQ**: [Frequently Asked Questions](docs/FAQ.md)

## 🔄 Roadmap

- [ ] Multi-language support (Swahili, French)
- [ ] iOS version
- [ ] Advanced analytics dashboard
- [ ] Social features and community
- [ ] Integration with betting platforms
- [ ] Offline mode improvements

## ⭐ Show Your Support

Give a ⭐️ if this project helped you!

## 👨‍💻 Authors

- **Your Name** - *Initial work* - [YourGitHub](https://github.com/yourusername)

## 🙏 Acknowledgments

- Firebase for backend services
- Material Design team for design guidelines
- Open source community for inspiration

---

**Disclaimer**: This app is for entertainment and educational purposes. Please gamble responsibly and in accordance with local laws.
