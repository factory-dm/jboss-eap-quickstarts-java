/**
 * keyboard-nav.js - Keyboard Navigation Enhancement for KitchenSink Application
 * 
 * This script improves keyboard accessibility by:
 * - Adding keyboard shortcuts for common actions
 * - Managing focus for interactive elements
 * - Providing accessibility features like skip-to-content
 * 
 * Complies with WCAG 2.1 keyboard accessibility guidelines
 */

(function() {
    'use strict';

    // Store references to commonly accessed elements
    let lastFocusedElement = null;
    let formElements = {};
    
    /**
     * Initialize keyboard navigation enhancements
     */
    function initKeyboardNavigation() {
        // Cache form elements for quick access
        cacheFormElements();
        
        // Add skip to content link
        addSkipToContentLink();
        
        // Set up event listeners
        document.addEventListener('keydown', handleKeyboardShortcuts);
        document.addEventListener('focusin', trackFocus);
        
        // If the page has a form, enhance it
        if (document.getElementById('reg')) {
            enhanceFormAccessibility();
        }
        
        console.log('Keyboard navigation enhancements initialized');
    }
    
    /**
     * Cache references to important form elements
     */
    function cacheFormElements() {
        const form = document.getElementById('reg');
        if (!form) return;
        
        formElements = {
            form: form,
            nameField: document.getElementById('reg:name'),
            emailField: document.getElementById('reg:email'),
            phoneField: document.getElementById('reg:phoneNumber'),
            registerButton: document.getElementById('reg:register')
        };
    }
    
    /**
     * Add a skip to content link at the top of the page
     * This allows keyboard users to bypass navigation elements
     */
    function addSkipToContentLink() {
        const skipLink = document.createElement('a');
        skipLink.href = '#content';
        skipLink.className = 'skip-to-content';
        skipLink.textContent = 'Skip to main content';
        skipLink.style.position = 'absolute';
        skipLink.style.top = '-40px';
        skipLink.style.left = '0';
        skipLink.style.padding = '8px';
        skipLink.style.zIndex = '100';
        skipLink.style.background = '#003c8c';
        skipLink.style.color = '#fff';
        skipLink.style.transition = 'top 0.3s';
        
        // Show the skip link when it receives focus
        skipLink.addEventListener('focus', function() {
            this.style.top = '0';
        });
        
        // Hide the skip link when it loses focus
        skipLink.addEventListener('blur', function() {
            this.style.top = '-40px';
        });
        
        // Insert at the beginning of the body
        document.body.insertBefore(skipLink, document.body.firstChild);
    }
    
    /**
     * Handle keyboard shortcuts
     * @param {KeyboardEvent} event - The keyboard event
     */
    function handleKeyboardShortcuts(event) {
        // Don't interfere with normal typing in form fields
        if (event.target.tagName === 'INPUT' && event.key !== 'Escape' && !event.altKey) {
            return;
        }
        
        // Alt key shortcuts
        if (event.altKey) {
            switch (event.key.toLowerCase()) {
                case 'r': // Alt+R: Focus on Register button
                    if (formElements.registerButton) {
                        event.preventDefault();
                        formElements.registerButton.focus();
                    }
                    break;
                    
                case 'n': // Alt+N: Focus on Name field
                    if (formElements.nameField) {
                        event.preventDefault();
                        formElements.nameField.focus();
                    }
                    break;
                    
                case 'e': // Alt+E: Focus on Email field
                    if (formElements.emailField) {
                        event.preventDefault();
                        formElements.emailField.focus();
                    }
                    break;
                    
                case 'p': // Alt+P: Focus on Phone field
                    if (formElements.phoneField) {
                        event.preventDefault();
                        formElements.phoneField.focus();
                    }
                    break;
            }
        }
        
        // Escape key to clear the current form field
        if (event.key === 'Escape') {
            const activeElement = document.activeElement;
            if (activeElement.tagName === 'INPUT' && activeElement.type !== 'submit' && activeElement.type !== 'button') {
                activeElement.value = '';
                event.preventDefault();
            }
        }
    }
    
    /**
     * Track the currently focused element
     * @param {FocusEvent} event - The focus event
     */
    function trackFocus(event) {
        lastFocusedElement = event.target;
    }
    
    /**
     * Restore focus to the last focused element
     */
    function restoreFocus() {
        if (lastFocusedElement && typeof lastFocusedElement.focus === 'function') {
            lastFocusedElement.focus();
        } else if (formElements.nameField) {
            // Default to the name field if no previous focus
            formElements.nameField.focus();
        }
    }
    
    /**
     * Enhance form accessibility
     */
    function enhanceFormAccessibility() {
        // Add helpful tooltips for keyboard shortcuts
        if (formElements.nameField) {
            formElements.nameField.title = 'Name field (Alt+N)';
        }
        
        if (formElements.emailField) {
            formElements.emailField.title = 'Email field (Alt+E)';
        }
        
        if (formElements.phoneField) {
            formElements.phoneField.title = 'Phone number field (Alt+P)';
        }
        
        if (formElements.registerButton) {
            formElements.registerButton.title = 'Register (Alt+R)';
        }
        
        // Listen for form submission to handle focus restoration
        if (formElements.form) {
            formElements.form.addEventListener('submit', function() {
                // Use setTimeout to allow the form processing to complete
                setTimeout(restoreFocus, 500);
            });
        }
    }
    
    // Initialize when the DOM is fully loaded
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initKeyboardNavigation);
    } else {
        initKeyboardNavigation();
    }
    
    // Expose public methods for potential use by other scripts
    window.keyboardNav = {
        restoreFocus: restoreFocus
    };
})();
