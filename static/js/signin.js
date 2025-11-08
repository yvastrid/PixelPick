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
            
            // Enviar datos al backend
            fetch('/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // Login exitoso, redirigir a welcome
                    window.location.href = '/welcome';
                } else {
                    if (data.requires_verification) {
                        // Mostrar opción de reenviar verificación
                        const resend = confirm(data.error + '\n\n¿Deseas que reenviemos el correo de verificación?');
                        if (resend) {
                            resendVerificationEmail(data.email);
                        }
                    } else {
                        alert(data.error || 'Error al iniciar sesión');
                    }
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al conectar con el servidor');
            });
        });
    }
});

// Función para reenviar correo de verificación
function resendVerificationEmail(email) {
    fetch('/api/resend-verification', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email: email })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message || 'Correo de verificación reenviado. Revisa tu bandeja de entrada.');
        } else {
            alert(data.error || 'Error al reenviar correo');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error al conectar con el servidor');
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

