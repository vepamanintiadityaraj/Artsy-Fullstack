
# 🎨 Artsy-Fullstack  

A **full-stack web and mobile platform** for exploring, searching, and managing art collections.  

---

## 🚀 Features  
- 🌐 **Cross-platform experience**: Accessible via web and mobile  
- 🌙 **Dark mode**: Built-in support for the Android app  
- 🔍 **Search functionality**: Quickly explore artworks with filters  
- 🔑 **Authentication**: Secure login/signup  
- ⚡ **Scalable backend**: Powered by Node.js & Express  
- ☁️ **Cloud database**: MongoDB Atlas integration  

---

## 🛠️ Tech Stack  
- **Frontend:** React.js, Bootstrap  
- **Mobile App:** Kotlin (Android Studio)  
- **Backend:** Node.js, Express.js  
- **Database:** MongoDB Atlas  
- **Deployment:** Google Cloud Platform (GCP)  

🔗 Live demo: [Artsy-Fullstack on GCP](https://adiartsytwt2.wl.r.appspot.com/)  

---

## 📂 Project Structure  
```bash
Artsy-Fullstack/
├── artsyApp_web/       # React frontend
├── artsyApp_Mobile/    # Kotlin mobile app
├── backend/            # Node.js + Express backend
├── database/           # Database configs & connection
│   ├── credentials.json   # Artsy API credentials
│   └── db.js             # MongoDB Atlas connection
└── README.md           # Documentation


⸻

⚙️ Setup & Installation

1️⃣ Clone the repository

git clone https://github.com/vepamanintiadityaraj/Artsy-Fullstack.git
cd Artsy-Fullstack

2️⃣ Backend Setup

cd backend
npm install
npm start

Create a .env file inside /backend with:

MONGO_URI=your-mongodb-atlas-uri
PORT=5000
JWT_SECRET=your-secret-key

Also, make sure you have a database/credentials.json file with your Artsy API credentials:

{
  "client_id": "your-artsy-client-id",
  "client_secret": "your-artsy-client-secret"
}

3️⃣ Frontend Setup

cd artsyApp_web
npm install
npm start

4️⃣ Mobile App Setup
	•	Open artsyApp_Mobile/ in Android Studio
	•	Configure SDK & Gradle if required
	•	Run the app on an emulator or device

⸻

🧪 Testing

Backend

npm test

Web frontend

npm run test


⸻

📦 Deployment
	•	Backend → GCP (App Engine / Cloud Run)
	•	Web frontend → GCP Hosting / Firebase
	•	Mobile app → Google Play Store

⸻

## 📸 App Demo  

🎥 [Watch Demo on Google Drive](https://drive.google.com/file/d/1f87_GESq4M96L7uMYPPppmkEDMdRGDaj/view?usp=sharing)  

⸻

👨‍💻 Contributors
	•	Aditya Raj Vepa – GitHub

⸻


