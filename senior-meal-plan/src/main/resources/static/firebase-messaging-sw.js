// 파일명: firebase-messaging-sw.js

// 1. 서비스 워커용 Firebase 라이브러리 임포트 (compat 버전 사용)
importScripts("https://www.gstatic.com/firebasejs/10.7.1/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/10.7.1/firebase-messaging-compat.js");

// 2. 아까 작성했던 설정값 그대로 붙여넣기
const firebaseConfig = {
    apiKey: "AIzaSyBUtGZ5A3MSDYjQnRguBx3dLuS9FKT2GQU",
    authDomain: "senior-meal-plan.firebaseapp.com",
    projectId: "senior-meal-plan",
    storageBucket: "senior-meal-plan.firebasestorage.app",
    messagingSenderId: "725290202600",
    appId: "1:725290202600:web:773c67b19c879435a1605d",
    measurementId: "G-YEECGP9T6J"
};

// 3. 앱 초기화 (백그라운드용)
firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

// 4. 백그라운드 메시지 핸들러 (선택사항이지만 에러 방지용)
messaging.onBackgroundMessage((payload) => {
    console.log('[firebase-messaging-sw.js] 백그라운드 메시지 수신 ', payload);
    // 알림 커스터마이징 가능
    const notificationTitle = payload.notification.title;
    const notificationOptions = {
        body: payload.notification.body,
        icon: '/firebase-logo.png'
    };

    self.registration.showNotification(notificationTitle, notificationOptions);
});