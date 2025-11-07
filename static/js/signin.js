// Sign In Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Password Toggle
    const passwordToggle = document.getElementById('passwordToggle');
    const passwordInput = document.getElementById('password');
    
    if (passwordToggle && passwordInput) {
        passwordToggle.addEventListener('click', function() {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            
            // Update icon (simple toggle - you can enhance this with different icons)
            passwordToggle.style.opacity = type === 'password' ? '0.5' : '1';
        });
    }

    // Form Submission
    const signinForm = document.getElementById('signinForm');
    
    if (signinForm) {
        signinForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Get form values
            const formData = {
                email: document.getElementById('email').value,
                password: document.getElementById('password').value
            };
            
            // Basic validation
            if (!formData.email || !formData.password) {
                alert('Por favor, completa todos los campos.');
                return;
            }
            
            // Email validation
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(formData.email)) {
                alert('Por favor, ingresa un correo electrónico válido.');
                return;
            }
            
            // Here you would typically send the data to your backend
            console.log('Sign in data:', formData);
            
            // Redirect to welcome page
            window.location.href = '/welcome';
        });
    }

    // Add input focus animations
    const inputs = document.querySelectorAll('.form-group input');
    
    inputs.forEach(input => {
        input.addEventListener('focus', function() {
            this.parentElement.style.transform = 'scale(1.02)';
        });
        
        input.addEventListener('blur', function() {
            this.parentElement.style.transform = 'scale(1)';
        });
    });
});

