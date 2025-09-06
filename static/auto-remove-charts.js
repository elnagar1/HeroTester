// Auto-remove Issue Types and Status Distribution charts
// Add this script to any page to automatically remove problematic charts

(function() {
    'use strict';
    
    console.log('Auto-removing Issue Types and Status Distribution charts...');
    
    // Function to remove chart elements
    function removeChartElements() {
        let removedCount = 0;
        
        // Remove by ID
        const elementsToRemove = [
            'statusChart',
            'issueTypesChart', 
            'statusChartCanvas',
            'issueTypesChartCanvas'
        ];
        
        elementsToRemove.forEach(id => {
            const element = document.getElementById(id);
            if (element) {
                element.remove();
                removedCount++;
                console.log(`Removed element with ID: ${id}`);
            }
        });
        
        // Remove by class
        const classesToRemove = [
            'chart-container',
            'compact-chart'
        ];
        
        classesToRemove.forEach(className => {
            const elements = document.querySelectorAll(`.${className}`);
            elements.forEach(element => {
                const text = element.textContent || '';
                if (text.includes('Status Distribution') || text.includes('Issue Types')) {
                    element.remove();
                    removedCount++;
                    console.log(`Removed element with class: ${className}`);
                }
            });
        });
        
        // Remove canvas elements that are too large
        const canvases = document.querySelectorAll('canvas');
        canvases.forEach(canvas => {
            if (canvas.height > 1000 || canvas.width > 1000) {
                canvas.remove();
                removedCount++;
                console.log('Removed oversized canvas');
            }
        });
        
        // Remove divs containing chart titles
        const allDivs = document.querySelectorAll('div');
        allDivs.forEach(div => {
            const text = div.textContent || '';
            if ((text.includes('Status Distribution') || text.includes('Issue Types')) && 
                (div.querySelector('canvas') || div.querySelector('.chart-container'))) {
                div.remove();
                removedCount++;
                console.log('Removed chart div');
            }
        });
        
        return removedCount;
    }
    
    // Function to remove chart-related CSS
    function removeChartCSS() {
        // Remove inline styles
        const elements = document.querySelectorAll('*');
        elements.forEach(element => {
            if (element.style && (element.id.includes('Chart') || element.className.includes('chart'))) {
                element.style.display = 'none';
            }
        });
    }
    
    // Main cleanup function
    function cleanup() {
        const removedCount = removeChartElements();
        removeChartCSS();
        
        if (removedCount > 0) {
            console.log(`Successfully removed ${removedCount} chart elements`);
        } else {
            console.log('No chart elements found to remove');
        }
    }
    
    // Run immediately
    cleanup();
    
    // Run after DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', cleanup);
    }
    
    // Run after a delay to catch dynamically loaded content
    setTimeout(cleanup, 1000);
    setTimeout(cleanup, 3000);
    
    console.log('Auto chart removal script loaded');
})();