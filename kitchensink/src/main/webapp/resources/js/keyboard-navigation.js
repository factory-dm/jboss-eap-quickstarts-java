/**
 * KitchenSink Keyboard Navigation and Accessibility Enhancements
 * 
 * This file implements comprehensive keyboard navigation and accessibility features
 * for the JBoss KitchenSink application, including keyboard shortcuts, focus management,
 * table navigation, and ARIA support for screen readers.
 * 
 * Implemented for Linear ticket FAC-22
 */

(function() {
  'use strict';

  //=============================================================================
  // Constants and Configuration
  //=============================================================================
  
  const KEYBOARD_SHORTCUTS = {
    // Format: [key, ctrlKey, altKey, shiftKey, description]
    REGISTER: ['r', true, false, false, 'Register a new member'],
    CLEAR: ['c', true, false, false, 'Clear the registration form'],
    TABLE: ['t', true, false, false, 'Navigate to members table'],
    HELP: ['h', true, false, false, 'Show keyboard shortcuts help']
  };

  const SELECTORS = {
    REGISTER_BUTTON: '#reg\\:register',
    FORM: '#reg',
    NAME_INPUT: '#reg\\:name',
    EMAIL_INPUT: '#reg\\:email',
    PHONE_INPUT: '#reg\\:phoneNumber',
    TABLE: '.simpletablestyle',
    TABLE_ROWS: '.simpletablestyle tr:not(:first-child)',
    CONTAINER: '#container',
    CONTENT: '#content',
    SKIP_LINK: '.skip-link',
    HELP_DIALOG: '#keyboard-help-dialog',
    FOCUS_TRAP_ELEMENTS: 'a[href], button, input, select, textarea, [tabindex]:not([tabindex="-1"])'
  };

  // ARIA live region messages
  const ARIA_MESSAGES = {
    FORM_CLEARED: 'Form has been cleared',
    MEMBER_REGISTERED: 'New member registered successfully',
    TABLE_NAVIGATION: 'Use arrow keys to navigate table. Press Enter to follow links.',
    HELP_OPENED: 'Help dialog opened. Press Escape to close.',
    HELP_CLOSED: 'Help dialog closed'
  };

  //=============================================================================
  // State Management
  //=============================================================================
  
  let state = {
    tableMode: false,            // Whether table navigation mode is active
    currentTableRow: -1,         // Current row in table navigation (-1 = none)
    currentTableCol: -1,         // Current column in table navigation
    helpDialogOpen: false,       // Whether help dialog is open
    lastFocusedElement: null,    // Element to restore focus to when closing modal
    ariaMessageQueue: [],        // Queue of messages for the ARIA live region
    formSubmitting: false        // Whether a form is currently being submitted
  };

  //=============================================================================
  // DOM Manipulation Utilities
  //=============================================================================
  
  /**
   * Creates and inserts DOM elements needed for accessibility features
   */
  function createAccessibilityElements() {
    // Create skip link
    const skipLink = document.createElement('a');
    skipLink.href = '#content';
    skipLink.className = 'skip-link';
    skipLink.textContent = 'Skip to main content';
    document.body.insertBefore(skipLink, document.body.firstChild);
    
    // Create ARIA live region for announcements
    const liveRegion = document.createElement('div');
    liveRegion.setAttribute('aria-live', 'polite');
    liveRegion.setAttribute('aria-atomic', 'true');
    liveRegion.className = 'visually-hidden';
    liveRegion.id = 'aria-live-announcer';
    document.body.appendChild(liveRegion);
    
    // Create help dialog
    createHelpDialog();
    
    // Add ARIA landmarks
    const content = document.querySelector(SELECTORS.CONTENT);
    if (content) {
      content.setAttribute('role', 'main');
      content.setAttribute('tabindex', '-1'); // Makes it focusable for skip links
    }
    
    const form = document.querySelector(SELECTORS.FORM);
    if (form) {
      form.setAttribute('role', 'form');
      form.setAttribute('aria-labelledby', 'reg-heading');
      
      // Add heading ID for ARIA labeling if not present
      const formHeading = form.querySelector('h2');
      if (formHeading && !formHeading.id) {
        formHeading.id = 'reg-heading';
      }
    }
    
    // Add table enhancements
    enhanceTableAccessibility();
  }
  
  /**
   * Creates the keyboard shortcuts help dialog
   */
  function createHelpDialog() {
    const helpDialog = document.createElement('div');
    helpDialog.id = 'keyboard-help-dialog';
    helpDialog.className = 'keyboard-help-dialog';
    helpDialog.setAttribute('role', 'dialog');
    helpDialog.setAttribute('aria-modal', 'true');
    helpDialog.setAttribute('aria-labelledby', 'help-dialog-title');
    helpDialog.setAttribute('aria-describedby', 'help-dialog-desc');
    helpDialog.setAttribute('tabindex', '-1');
    helpDialog.style.display = 'none';
    
    // Dialog content
    helpDialog.innerHTML = `
      <div class="dialog-content">
        <h2 id="help-dialog-title">Keyboard Shortcuts</h2>
        <p id="help-dialog-desc">The following keyboard shortcuts are available:</p>
        <ul>
          <li><kbd>Ctrl</kbd> + <kbd>R</kbd>: ${KEYBOARD_SHORTCUTS.REGISTER[4]}</li>
          <li><kbd>Ctrl</kbd> + <kbd>C</kbd>: ${KEYBOARD_SHORTCUTS.CLEAR[4]}</li>
          <li><kbd>Ctrl</kbd> + <kbd>T</kbd>: ${KEYBOARD_SHORTCUTS.TABLE[4]}</li>
          <li><kbd>Ctrl</kbd> + <kbd>H</kbd>: ${KEYBOARD_SHORTCUTS.HELP[4]}</li>
        </ul>
        <p>Table Navigation:</p>
        <ul>
          <li><kbd>↑</kbd> / <kbd>↓</kbd>: Navigate between rows</li>
          <li><kbd>←</kbd> / <kbd>→</kbd>: Navigate between columns</li>
          <li><kbd>Enter</kbd>: Activate links in current cell</li>
          <li><kbd>Esc</kbd>: Exit table navigation mode</li>
        </ul>
        <button id="close-help-dialog" class="close-button">Close</button>
      </div>
    `;
    
    document.body.appendChild(helpDialog);
    
    // Add event listener for the close button
    document.getElementById('close-help-dialog').addEventListener('click', closeHelpDialog);
  }
  
  /**
   * Enhances table accessibility with ARIA roles and keyboard navigation support
   */
  function enhanceTableAccessibility() {
    const table = document.querySelector(SELECTORS.TABLE);
    if (!table) return;
    
    // Add ARIA roles
    table.setAttribute('role', 'grid');
    table.setAttribute('aria-label', 'Members List');
    
    // Add row and cell roles
    const rows = table.querySelectorAll('tr');
    rows.forEach((row, rowIndex) => {
      row.setAttribute('role', 'row');
      row.setAttribute('tabindex', rowIndex === 1 ? '0' : '-1'); // First data row is focusable
      
      // Add keyboard navigation attributes to cells
      const cells = row.querySelectorAll('td, th');
      cells.forEach((cell, colIndex) => {
        cell.setAttribute('role', cell.tagName.toLowerCase() === 'th' ? 'columnheader' : 'gridcell');
        cell.setAttribute('tabindex', '-1');
        cell.setAttribute('data-row', rowIndex.toString());
        cell.setAttribute('data-col', colIndex.toString());
      });
    });
    
    // Add table instructions for screen readers
    const tableCaption = document.createElement('caption');
    tableCaption.className = 'visually-hidden';
    tableCaption.textContent = 'Press Ctrl+T to enter table navigation mode. Then use arrow keys to navigate.';
    
    // Insert as first child of table
    if (table.firstChild) {
      table.insertBefore(tableCaption, table.firstChild);
    } else {
      table.appendChild(tableCaption);
    }
  }

  //=============================================================================
  // Focus Management
  //=============================================================================
  
  /**
   * Manages focus for modal dialogs by trapping focus within the modal
   * @param {HTMLElement} modalElement - The modal element to trap focus within
   */
  function trapFocusInModal(modalElement) {
    state.lastFocusedElement = document.activeElement;
    
    const focusableElements = modalElement.querySelectorAll(SELECTORS.FOCUS_TRAP_ELEMENTS);
    const firstFocusableElement = focusableElements[0];
    const lastFocusableElement = focusableElements[focusableElements.length - 1];
    
    // Focus the first element
    setTimeout(() => {
      firstFocusableElement.focus();
    }, 50);
    
    // Handle tab key to keep focus inside the modal
    modalElement.addEventListener('keydown', function handleTabKey(e) {
      if (e.key === 'Tab') {
        // Shift + Tab
        if (e.shiftKey && document.activeElement === firstFocusableElement) {
          e.preventDefault();
          lastFocusableElement.focus();
        }
        // Tab
        else if (!e.shiftKey && document.activeElement === lastFocusableElement) {
          e.preventDefault();
          firstFocusableElement.focus();
        }
      }
      
      // Close on Escape
      if (e.key === 'Escape') {
        closeHelpDialog();
      }
    });
  }
  
  /**
   * Restores focus to the previously focused element
   */
  function restoreFocus() {
    if (state.lastFocusedElement) {
      state.lastFocusedElement.focus();
      state.lastFocusedElement = null;
    }
  }
  
  /**
   * Handles focus for table navigation
   * @param {number} rowIndex - The row index to focus
   * @param {number} colIndex - The column index to focus
   */
  function focusTableCell(rowIndex, colIndex) {
    const table = document.querySelector(SELECTORS.TABLE);
    if (!table) return;
    
    const rows = table.querySelectorAll('tr');
    
    // Validate indices
    if (rowIndex < 0) rowIndex = 0;
    if (rowIndex >= rows.length) rowIndex = rows.length - 1;
    
    const row = rows[rowIndex];
    const cells = row.querySelectorAll('td, th');
    
    if (colIndex < 0) colIndex = 0;
    if (colIndex >= cells.length) colIndex = cells.length - 1;
    
    const cell = cells[colIndex];
    
    // Update state
    state.currentTableRow = rowIndex;
    state.currentTableCol = colIndex;
    
    // Focus the cell
    cell.focus();
    
    // Announce for screen readers
    if (rowIndex === 0) {
      announceToScreenReader(`Column header: ${cell.textContent.trim()}`);
    } else {
      const columnHeader = rows[0].querySelectorAll('th')[colIndex].textContent.trim();
      announceToScreenReader(`Row ${rowIndex}, ${columnHeader}: ${cell.textContent.trim()}`);
    }
  }

  //=============================================================================
  // ARIA and Screen Reader Support
  //=============================================================================
  
  /**
   * Announces a message to screen readers via the ARIA live region
   * @param {string} message - The message to announce
   */
  function announceToScreenReader(message) {
    const liveRegion = document.getElementById('aria-live-announcer');
    if (!liveRegion) return;
    
    // Queue the message
    state.ariaMessageQueue.push(message);
    
    // Process the queue
    if (state.ariaMessageQueue.length === 1) {
      processAriaMessageQueue();
    }
  }
  
  /**
   * Processes the queue of ARIA messages
   */
  function processAriaMessageQueue() {
    if (state.ariaMessageQueue.length === 0) return;
    
    const liveRegion = document.getElementById('aria-live-announcer');
    if (!liveRegion) return;
    
    const message = state.ariaMessageQueue[0];
    liveRegion.textContent = message;
    
    // Remove the message after a delay and process the next one
    setTimeout(() => {
      state.ariaMessageQueue.shift();
      if (state.ariaMessageQueue.length > 0) {
        processAriaMessageQueue();
      }
    }, 1000);
  }

  //=============================================================================
  // Keyboard Shortcut Handlers
  //=============================================================================
  
  /**
   * Handles the Register keyboard shortcut (Ctrl+R)
   */
  function handleRegisterShortcut() {
    const registerButton = document.querySelector(SELECTORS.REGISTER_BUTTON);
    if (registerButton) {
      registerButton.focus();
      registerButton.click();
      announceToScreenReader(ARIA_MESSAGES.MEMBER_REGISTERED);
    }
  }
  
  /**
   * Handles the Clear keyboard shortcut (Ctrl+C)
   */
  function handleClearShortcut() {
    const form = document.querySelector(SELECTORS.FORM);
    if (form) {
      const inputs = form.querySelectorAll('input[type="text"]');
      inputs.forEach(input => {
        input.value = '';
      });
      
      // Focus the first input
      const firstInput = document.querySelector(SELECTORS.NAME_INPUT);
      if (firstInput) {
        firstInput.focus();
      }
      
      announceToScreenReader(ARIA_MESSAGES.FORM_CLEARED);
    }
  }
  
  /**
   * Handles the Table Navigation keyboard shortcut (Ctrl+T)
   */
  function handleTableShortcut() {
    const table = document.querySelector(SELECTORS.TABLE);
    if (!table) return;
    
    // Toggle table navigation mode
    state.tableMode = !state.tableMode;
    
    if (state.tableMode) {
      // Enter table navigation mode
      state.currentTableRow = 1; // First data row
      state.currentTableCol = 0; // First column
      
      focusTableCell(state.currentTableRow, state.currentTableCol);
      announceToScreenReader(ARIA_MESSAGES.TABLE_NAVIGATION);
      
      // Add visual indicator
      table.classList.add('table-navigation-active');
    } else {
      // Exit table navigation mode
      table.classList.remove('table-navigation-active');
      
      // Focus the table itself
      table.focus();
    }
  }
  
  /**
   * Handles the Help keyboard shortcut (Ctrl+H)
   */
  function handleHelpShortcut() {
    const helpDialog = document.querySelector(SELECTORS.HELP_DIALOG);
    if (!helpDialog) return;
    
    // Toggle help dialog
    if (state.helpDialogOpen) {
      closeHelpDialog();
    } else {
      openHelpDialog();
    }
  }
  
  /**
   * Opens the keyboard shortcuts help dialog
   */
  function openHelpDialog() {
    const helpDialog = document.querySelector(SELECTORS.HELP_DIALOG);
    if (!helpDialog) return;
    
    helpDialog.style.display = 'block';
    state.helpDialogOpen = true;
    
    // Trap focus in the dialog
    trapFocusInModal(helpDialog);
    
    announceToScreenReader(ARIA_MESSAGES.HELP_OPENED);
  }
  
  /**
   * Closes the keyboard shortcuts help dialog
   */
  function closeHelpDialog() {
    const helpDialog = document.querySelector(SELECTORS.HELP_DIALOG);
    if (!helpDialog) return;
    
    helpDialog.style.display = 'none';
    state.helpDialogOpen = false;
    
    // Restore focus
    restoreFocus();
    
    announceToScreenReader(ARIA_MESSAGES.HELP_CLOSED);
  }

  //=============================================================================
  // Event Handlers
  //=============================================================================
  
  /**
   * Handles keydown events for keyboard shortcuts and navigation
   * @param {KeyboardEvent} event - The keydown event
   */
  function handleKeyDown(event) {
    // Help dialog is open - only handle Escape
    if (state.helpDialogOpen) {
      if (event.key === 'Escape') {
        closeHelpDialog();
        event.preventDefault();
      }
      return;
    }
    
    // Table navigation mode
    if (state.tableMode) {
      handleTableNavigation(event);
      return;
    }
    
    // Global keyboard shortcuts
    if (event.ctrlKey && !event.altKey && !event.shiftKey) {
      switch (event.key.toLowerCase()) {
        case KEYBOARD_SHORTCUTS.REGISTER[0]:
          handleRegisterShortcut();
          event.preventDefault();
          break;
          
        case KEYBOARD_SHORTCUTS.CLEAR[0]:
          handleClearShortcut();
          event.preventDefault();
          break;
          
        case KEYBOARD_SHORTCUTS.TABLE[0]:
          handleTableShortcut();
          event.preventDefault();
          break;
          
        case KEYBOARD_SHORTCUTS.HELP[0]:
          handleHelpShortcut();
          event.preventDefault();
          break;
      }
    }
  }
  
  /**
   * Handles keyboard navigation within the table
   * @param {KeyboardEvent} event - The keydown event
   */
  function handleTableNavigation(event) {
    const table = document.querySelector(SELECTORS.TABLE);
    if (!table) return;
    
    let newRow = state.currentTableRow;
    let newCol = state.currentTableCol;
    
    switch (event.key) {
      case 'ArrowUp':
        newRow--;
        event.preventDefault();
        break;
        
      case 'ArrowDown':
        newRow++;
        event.preventDefault();
        break;
        
      case 'ArrowLeft':
        newCol--;
        event.preventDefault();
        break;
        
      case 'ArrowRight':
        newCol++;
        event.preventDefault();
        break;
        
      case 'Escape':
        // Exit table navigation mode
        state.tableMode = false;
        table.classList.remove('table-navigation-active');
        table.focus();
        announceToScreenReader('Exited table navigation mode');
        event.preventDefault();
        return;
        
      case 'Enter':
        // Activate links in the current cell
        const cell = table.querySelector(`tr:nth-child(${state.currentTableRow + 1}) td:nth-child(${state.currentTableCol + 1})`);
        if (cell) {
          const link = cell.querySelector('a');
          if (link) {
            link.click();
          }
        }
        event.preventDefault();
        return;
    }
    
    // Focus the new cell if indices changed
    if (newRow !== state.currentTableRow || newCol !== state.currentTableCol) {
      focusTableCell(newRow, newCol);
    }
  }
  
  /**
   * Handles form submission to restore focus after submission
   * @param {Event} event - The submit event
   */
  function handleFormSubmit(event) {
    // Remember the active element to restore focus after submission
    state.lastFocusedElement = document.activeElement;
    state.formSubmitting = true;
  }
  
  /**
   * Handles form reset (clear) to announce to screen readers
   * @param {Event} event - The reset event
   */
  function handleFormReset(event) {
    announceToScreenReader(ARIA_MESSAGES.FORM_CLEARED);
  }

  //=============================================================================
  // Initialization
  //=============================================================================
  
  /**
   * Initializes all keyboard navigation and accessibility features
   */
  function init() {
    // Create necessary DOM elements
    createAccessibilityElements();
    
    // Add keyboard shortcut indicators to UI
    addKeyboardShortcutIndicators();
    
    // Add event listeners
    document.addEventListener('keydown', handleKeyDown);
    
    // Form event listeners
    const form = document.querySelector(SELECTORS.FORM);
    if (form) {
      form.addEventListener('submit', handleFormSubmit);
      form.addEventListener('reset', handleFormReset);
    }
    
    // Add focus restoration after AJAX updates
    setupFocusRestorationAfterAjax();
    
    console.log('KitchenSink keyboard navigation initialized');
  }
  
  /**
   * Adds visual indicators for keyboard shortcuts to the UI
   */
  function addKeyboardShortcutIndicators() {
    // Add to Register button
    const registerButton = document.querySelector(SELECTORS.REGISTER_BUTTON);
    if (registerButton) {
      const shortcutSpan = document.createElement('span');
      shortcutSpan.className = 'kbd-shortcut';
      shortcutSpan.setAttribute('aria-hidden', 'true'); // Hide from screen readers
      shortcutSpan.textContent = 'Ctrl+R';
      registerButton.appendChild(shortcutSpan);
    }
    
    // Add to form heading
    const formHeading = document.querySelector('#reg h2');
    if (formHeading) {
      // Add clear shortcut
      const clearShortcut = document.createElement('span');
      clearShortcut.className = 'kbd-shortcut';
      clearShortcut.setAttribute('aria-hidden', 'true');
      clearShortcut.textContent = 'Ctrl+C to clear';
      formHeading.appendChild(clearShortcut);
    }
    
    // Add to table heading
    const tableHeading = document.querySelector('h2:contains("Members")');
    if (tableHeading) {
      const tableShortcut = document.createElement('span');
      tableShortcut.className = 'kbd-shortcut';
      tableShortcut.setAttribute('aria-hidden', 'true');
      tableShortcut.textContent = 'Ctrl+T';
      tableHeading.appendChild(tableShortcut);
    }
  }
  
  /**
   * Sets up focus restoration after AJAX updates
   */
  function setupFocusRestorationAfterAjax() {
    // This is a placeholder for JSF/Ajax integration
    // In a real implementation, this would hook into the JSF Ajax lifecycle
    
    // For JSF 2.0+, you might use:
    if (typeof jsf !== 'undefined' && jsf.ajax) {
      jsf.ajax.addOnEvent(function(data) {
        if (data.status === 'success') {
          // Restore focus after successful ajax update
          if (state.formSubmitting) {
            // Focus the name input after registration
            const nameInput = document.querySelector(SELECTORS.NAME_INPUT);
            if (nameInput) {
              setTimeout(() => {
                nameInput.focus();
                nameInput.select();
              }, 100);
            }
            state.formSubmitting = false;
          }
        }
      });
    }
  }
  
  // Initialize when the DOM is fully loaded
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
