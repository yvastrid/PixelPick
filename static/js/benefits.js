// Benefits page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    const mode = new URLSearchParams(window.location.search).get('mode') || 'view';
    const selectBasicButton = document.getElementById('selectBasicButton');
    const purchaseButton = document.getElementById('purchaseButton');
    const basicPlanCard = document.querySelector('.basic-plan');
    const premiumPlanCard = document.querySelector('.premium-plan');
    
    console.log('Benefits page loaded. Mode:', mode);
    
    // Load current subscription status
    loadSubscriptionStatus();
    
    // Setup button listeners
    if (selectBasicButton) {
        selectBasicButton.addEventListener('click', function() {
            activateBasicPlan();
        });
    }
    
    if (purchaseButton) {
        purchaseButton.addEventListener('click', function() {
            activatePremiumPlan();
        });
    }
    
    // Apply visual state based on mode and subscription
    function applyVisualState(subscription) {
        if (mode === 'upgrade') {
            // Upgrade mode: show the opposite plan
            const planType = subscription ? subscription.plan_type : null;
            if (!planType || planType.includes('basic')) {
                // Has basic or no subscription -> show premium for upgrade
                applyUpgradeModeBasic();
            } else if (planType === 'pixelie_plan' || planType.includes('pixelie_plan')) {
                // Has premium -> show basic blocked
                applyUpgradeModePremium();
            }
        } else {
            // View mode: show active plan
            const planType = subscription ? subscription.plan_type : null;
            if (planType && planType.includes('basic')) {
                applyBasicPlanSelectedState();
            } else if (planType === 'pixelie_plan' || planType.includes('pixelie_plan')) {
                applyPremiumPlanSelectedState();
            }
        }
    }
    
    function applyUpgradeModeBasic() {
        // User has basic -> show premium for upgrade
        if (basicPlanCard) basicPlanCard.style.opacity = '1';
        if (premiumPlanCard) premiumPlanCard.style.opacity = '1';
        if (selectBasicButton) {
            selectBasicButton.style.display = 'none';
        }
    }
    
    function applyUpgradeModePremium() {
        // User has premium -> show basic blocked
        if (basicPlanCard) {
            basicPlanCard.style.opacity = '0.5';
            basicPlanCard.style.pointerEvents = 'none';
        }
        if (premiumPlanCard) premiumPlanCard.style.opacity = '1';
        if (selectBasicButton) {
            selectBasicButton.textContent = 'Plan Actual';
            selectBasicButton.disabled = true;
        }
    }
    
    function applyBasicPlanSelectedState() {
        // Basic plan is active
        if (basicPlanCard) {
            basicPlanCard.style.border = '2px solid #00d4ff';
            basicPlanCard.style.opacity = '1';
        }
        if (selectBasicButton) {
            selectBasicButton.textContent = 'Seleccionado';
            selectBasicButton.disabled = true;
        }
        if (premiumPlanCard) {
            premiumPlanCard.style.display = 'none';
        }
    }
    
    function applyPremiumPlanSelectedState() {
        // Premium plan is active
        if (premiumPlanCard) {
            premiumPlanCard.style.border = '2px solid #00d4ff';
            premiumPlanCard.style.opacity = '1';
        }
        if (purchaseButton) {
            purchaseButton.textContent = 'Plan Actual';
            purchaseButton.disabled = true;
        }
        if (basicPlanCard) {
            basicPlanCard.style.display = 'none';
        }
    }
    
    function loadSubscriptionStatus() {
        fetch('/api/subscription/status')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                console.log('Subscription status:', data);
                if (data.success && data.user) {
                    const subscription = data.user.subscription;
                    if (subscription) {
                        applyVisualState(subscription);
                        updateMembershipDetails(subscription);
                    } else {
                        // No subscription, default to basic
                        applyVisualState(null);
                    }
                }
            })
            .catch(error => {
                console.error('Error loading subscription status:', error);
            });
    }
    
    function updateMembershipDetails(subscription) {
        const activePlanName = document.getElementById('activePlanName');
        const activePlanStatus = document.getElementById('activePlanStatus');
        
        if (activePlanName && subscription) {
            if (subscription.plan_type === 'pixelie_plan') {
                activePlanName.textContent = 'Pixelie Plan';
            } else {
                activePlanName.textContent = 'Pixelie Basic Plan';
            }
        }
        
        if (activePlanStatus && subscription) {
            if (subscription.status === 'active') {
                activePlanStatus.textContent = 'Activo';
            } else {
                activePlanStatus.textContent = subscription.status.charAt(0).toUpperCase() + subscription.status.slice(1);
            }
        }
    }
    
    function activateBasicPlan() {
        fetch('/api/subscription/activate-basic', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('¡Plan básico activado exitosamente!');
                // Reload page after a short delay
                setTimeout(() => {
                    window.location.href = '/welcome';
                }, 1000);
            } else {
                alert('Error al activar plan básico: ' + (data.error || 'Error desconocido'));
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error al activar plan básico');
        });
    }
    
    function activatePremiumPlan() {
        fetch('/api/subscription/activate-premium', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('¡Plan premium activado exitosamente!');
                // Reload page after a short delay
                setTimeout(() => {
                    window.location.href = '/welcome';
                }, 1000);
            } else {
                alert('Error al activar plan premium: ' + (data.error || 'Error desconocido'));
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error al activar plan premium');
        });
    }
});

