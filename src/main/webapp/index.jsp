<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Welcome - GraceDM</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Official document management system for Grace Intercessory Healing and Deliverance Ministry">
    <meta name="keywords" content="Grace Intercessory Healing and Deliverance Ministry documents, gracedm">
    
    <style>
        :root {
            --primary-blue: #2196F3;
            --dark-blue: #1976D2;
            --light-blue: #e3f2fd;
            --white: #ffffff;
            --text-color: #333;
            --gray: #f5f5f5;
        }
        
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Arial', sans-serif;
        }
        
        body {
            background-color: var(--gray);
            color: var(--text-color);
            line-height: 1.6;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 20px;
        }
        
        header {
            background-color: var(--primary-blue);
            color: var(--white);
            padding: 1rem 0;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        
        nav {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .logo {
            font-size: 1.8rem;
            font-weight: bold;
            color: var(--white);
            text-decoration: none;
        }
        
        .hero {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 2rem;
            align-items: center;
            padding: 4rem 0;
        }
        
        .hero-content h1 {
            font-size: 2.2rem;
            margin-bottom: 1rem;
            color: var(--dark-blue);
        }
        
        .hero-content p {
            font-size: 1.1rem;
            margin-bottom: 2rem;
            color: var(--text-color);
        }
        
        .login-box {
            background: var(--white);
            padding: 2rem;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            text-align: center;
            margin-top: 2rem;
            border-top: 4px solid var(--dark-blue);
        }
        
        .login-title {
            color: var(--dark-blue);
            margin-bottom: 1.5rem;
            font-size: 1.5rem;
        }
        
        .google-btn img {
            width: 100%; 
            max-width: 240px;
            height: auto;
            transition: opacity 0.3s;
        }
        
        .google-btn img:hover {
            opacity: 0.9;
        }
        
        .ministry-highlights {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 2rem;
            padding: 3rem 0;
        }
        
        .ministry-card {
            background: var(--white);
            padding: 1.5rem;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.05);
            border-left: 4px solid var(--primary-blue);
        }
        
        .ministry-card h3 {
            color: var(--dark-blue);
            margin-bottom: 0.5rem;
        }
        
        footer {
            background-color: var(--dark-blue);
            color: var(--white);
            padding: 2rem 0;
            text-align: center;
            margin-top: 3rem;
        }
        
        .church-photo {
            width: 100%;
            border-radius: 8px;
            max-height: 400px;
            object-fit: cover;
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        
        @media (max-width: 768px) {
            .hero {
                grid-template-columns: 1fr;
                text-align: center;
                padding: 2rem 0;
            }
            
            .hero-content h1 {
                font-size: 1.8rem;
            }
        }
    </style>
</head>
<body>
    <header>
        <div class="container">
            <nav>
                <a href="#" class="logo">GraceDM</a>
            </nav>
        </div>
    </header>

    <main class="container">
        <section class="hero">
            <div class="hero-content">
                <h1>Welcome to Grace Intercessory Healing and Deliverance Ministry Document Manager</h1>
                <p>GraceDM is our secure platform for managing our church documents including Service materials, Videos, and Pictures.</p>
                
                <div class="login-box">
                    <h2 class="login-title">Ministry Access</h2>
                    <p style="margin-bottom: 1rem;">For authorized members only</p>
                    <div style="margin: 1.5rem 0;">
                        <a href="${pageContext.request.contextPath}/auth/google" class="google-btn">
                            <img src="https://developers.google.com/identity/images/btn_google_signin_dark_normal_web.png" 
                                 alt="Sign in with Google">
                        </a>
                    </div>
                    <p style="font-size: 0.9rem; color: #555;">Use your authorized google email</p>
                </div>
             </div>
            
             <div>
                <img src="${pageContext.request.contextPath}/images/church-photo.jpg" 
                     alt="GIHDM" style="width: 100%; border-radius: 8px;" class="church-photo">
            </div>
        </section>

        <section class="ministry-highlights">
            <div class="ministry-card">
                <h3>Ministry Videos</h3>
                <p>Sermons, Service Highlights, and Events recordings</p>
            </div>
            
            <div class="ministry-card">
                <h3>Pictures</h3>
                <p>Services, Programs, and Others photos</p>
            </div>
            
            <div class="ministry-card">
                <h3>Documents</h3>
                <p>Letters, Certificates, and Programs Sheets for different occasions</p>
            </div>
        </section>
    </main>

    <footer>
        <div class="container">
            <p>&copy; 2025 Grace Intercessory Healing and Deliverance Ministry</p>
            <p style="margin-top: 0.5rem; font-size: 0.9rem;">"Call the Sinners to Repentance" (Luke 5:32)</p>
        </div>
    </footer>
</body>
</html>