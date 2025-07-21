/**
 * keyboard-nav.js - Enhanced Keyboard Navigation for KitchenSink Application
 * 
 * This script improves keyboard accessibility by implementing:
 * - Additional keyboard shortcuts
 * - Enhanced table navigation
 * - Focus management
 * - Screen reader announcements
 * 
 * Follows WCAG 2.1 guidelines for keyboard accessibility
 */
(function() {
    'use strict';

    // ====================================================
    // Screen Reader Announcement Utility
    // ====================================================
    
    /**
     * Creates and manages a live region for screen reader announcements
     */
    const ScreenReaderAnnouncer = {
        liveRegion: null,
        
        /**
         * Initialize the live region if it doesn't exist
         */
        init: function() {
            if (!this.liveRegion) {
                this.liveRegion = document.createElement('div');
                this.liveRegion.setAttribute('aria-live', 'polite');
                this.liveRegion.setAttribute('aria-atomic', 'true');
                this.liveRegion.setAttribute('class', 'visually-hidden');
                document.body.appendChild(this.liveRegion);
            }
        },
        
        /**
         * Announce a message to screen readers
         * @param {string} message - The message to announce
         * @param {string} priority - The priority level ('polite' or 'assertive')
         */
        announce: function(message, priority = 'polite') {
            this.init();
            this.liveRegion.setAttribute('aria-live', priority);
            
            // Clear the region first to ensure announcement
            this.liveRegion.textContent = '';
            
            // Use setTimeout to ensure the DOM update is recognized
            setTimeout(() => {
                this.liveRegion.textContent = message;
            }, 50);
        }
    };

    // ====================================================
    // Keyboard Shortcut Handlers
    // ====================================================
    
    /**
     * Registers and handles global keyboard shortcuts
     */
    function setupKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            // Skip to content: Alt + 1
            if (e.altKey && !e.shiftKey && !e.ctrlKey && !e.metaKey && e.key === '1') {
                const mainContent = document.getElementById('main-content');
                if (mainContent) {
                    mainContent.focus();
                    e.preventDefault();
                    ScreenReaderAnnouncer.announce('Navigated to main content');
                }
            }
            
            // Register button shortcut already implemented in default.xhtml
            // This is just a reference to that functionality
            
            // Escape key handler for closing dialogs or exiting focus traps
            if (e.key === 'Escape') {
                const activeElement = document.activeElement;
                // If we're in a form field and not in a select dropdown
                if (activeElement && 
                    (activeElement.tagName === 'INPUT' || 
                     activeElement.tagName === 'TEXTAREA')) {
                    activeElement.blur();
                    e.preventDefault();
                }
            }
        });
    }

    // ====================================================
    // Table Navigation Enhancement
    // ====================================================
    
    /**
     * Enhances data tables with keyboard navigation using arrow keys
     */
    function enhanceTableNavigation() {
        const tables = document.querySelectorAll('.simpletablestyle');
        
        tables.forEach(table => {
            // Make table focusable if it isn't already
            if (!table.hasAttribute('tabindex')) {
                table.setAttribute('tabindex', '0');
            }
            
            // Set up keyboard navigation within the table
            table.addEventListener('keydown', function(e) {
                // Only handle arrow keys
                if (!['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.key)) {
                    return;
                }
                
                // Get all cells in the table
                const rows = Array.from(table.querySelectorAll('tr'));
                
                // Skip header row
                const dataRows = rows.slice(1);
                
                // If no rows, exit
                if (dataRows.length === 0) return;
                
                // Find the currently focused cell or start with the first one
                let focusedCell = document.activeElement;
                let currentRow = -1;
                let currentCol = -1;
                
                // If the table itself has focus, start at the first cell
                if (focusedCell === table) {
                    currentRow = 0;
                    currentCol = 0;
                } else {
                    // Find the current cell position
                    for (let i = 0; i < dataRows.length; i++) {
                        const cells = Array.from(dataRows[i].querySelectorAll('td'));
                        const cellIndex = cells.indexOf(focusedCell);
                        
                        if (cellIndex !== -1) {
                            currentRow = i;
                            currentCol = cellIndex;
                            break;
                        }
                    }
                }
                
                // Calculate new position based on arrow key
                switch (e.key) {
                    case 'ArrowUp':
                        currentRow = Math.max(0, currentRow - 1);
                        break;
                    case 'ArrowDown':
                        currentRow = Math.min(dataRows.length - 1, currentRow + 1);
                        break;
                    case 'ArrowLeft':
                        currentCol = Math.max(0, currentCol - 1);
                        break;
                    case 'ArrowRight':
                        const cells = Array.from(dataRows[currentRow].querySelectorAll('td'));
                        currentCol = Math.min(cells.length - 1, currentCol + 1);
                        break;
                }
                
                // Focus the new cell
                if (currentRow >= 0 && currentCol >= 0) {
                    const newRow = dataRows[currentRow];
                    const cells = Array.from(newRow.querySelectorAll('td'));
                    
                    if (cells[currentCol]) {
                        // If cell contains a link, focus that instead
                        const link = cells[currentCol].querySelector('a');
                        if (link) {
                            link.focus();
                        } else {
                            cells[currentCol].setAttribute('tabindex', '-1');
                            cells[currentCol].focus();
                        }
                        e.preventDefault();
                    }
                }
            });
        });
    }

    // ====================================================
    // Focus Management
    // ====================================================
    
    /**
     * Manages focus trapping within forms when needed
     */
    function setupFocusManagement() {
        const registrationForm = document.getElementById('reg');
        
        if (registrationForm) {
            const formElements = Array.from(
                registrationForm.querySelectorAll('input, button, select, textarea, a')
            ).filter(el => !el.disabled && el.type !== 'hidden');
            
            // If the form is in an error state, trap focus within it
            const hasErrors = registrationForm.querySelector('.invalid');
            
            if (hasErrors) {
                // Set focus to the first field with an error
                const firstErrorField = registrationForm.querySelector('input:invalid');
                if (firstErrorField) {
                    firstErrorField.focus();
                    ScreenReaderAnnouncer.announce('Form contains errors. Please correct them and try again.');
                }
                
                // Add event listener to keep focus within the form
                registrationForm.addEventListener('keydown', function(e) {
                    // If Tab key is pressed
                    if (e.key === 'Tab') {
                        const firstFocusableEl = formElements[0];
                        const lastFocusableEl = formElements[formElements.length - 1];
                        
                        // If Shift+Tab on first element, wrap to last
                        if (e.shiftKey && document.activeElement === firstFocusableEl) {
                            lastFocusableEl.focus();
                            e.preventDefault();
                        }
                        // If Tab on last element, wrap to first
                        else if (!e.shiftKey && document.activeElement === lastFocusableEl) {
                            firstFocusableEl.focus();
                            e.preventDefault();
                        }
                    }
                });
            }
        }
    }

    // ====================================================
    // Form Submission Feedback
    // ====================================================
    
    /**
     * Provides feedback when forms are submitted
     */
    function setupFormFeedback() {
        const registrationForm = document.getElementById('reg');
        
        if (registrationForm) {
            registrationForm.addEventListener('submit', function() {
                ScreenReaderAnnouncer.announce('Form submitted. Processing your registration...');
            });
            
            // Find the register button
            const registerButton = document.getElementById('reg:register');
            if (registerButton) {
                registerButton.addEventListener('click', function() {
                    // This will fire before the form submission event
                    // We add a small delay to ensure the announcement isn't overridden
                    setTimeout(() => {
                        const messages = document.getElementById('globalMsgs');
                        if (messages && messages.textContent.trim()) {
                            // If there are messages, they will be announced by the aria-live region
                            // No need to duplicate announcements
                        } else {
                            // Only announce if no messages were displayed
                            ScreenReaderAnnouncer.announce('Processing your registration...');
                        }
                    }, 100);
                });
            }
        }
    }

    // ====================================================
    // Initialization
    // ====================================================
    
    /**
     * Initialize all keyboard navigation enhancements when the DOM is ready
     */
    function init() {
        // Initialize screen reader announcer
        ScreenReaderAnnouncer.init();
        
        // Set up keyboard shortcuts
        setupKeyboardShortcuts();
        
        // Enhance table navigation
        enhanceTableNavigation();
        
        // Set up focus management
        setupFocusManagement();
        
        // Set up form feedback
        setupFormFeedback();
        
        // Announce page is ready for keyboard navigation
        ScreenReaderAnnouncer.announce('Page loaded. Keyboard navigation is available. Use Alt+1 to skip to content or Alt+R to jump to the register button.');
    }

    // Run initialization when DOM is fully loaded
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
