// Script to remove Issue Types and Status Distribution charts
// This script will remove the problematic chart sections from any page

(function() {
    'use strict';
    
    // Function to remove chart sections
    function removeChartSections() {
        // Remove Status Distribution section
        const statusChart = document.getElementById('statusChart');
        if (statusChart) {
            statusChart.remove();
            console.log('Removed Status Distribution chart');
        }
        
        // Remove Issue Types section
        const issueTypesChart = document.getElementById('issueTypesChart');
        if (issueTypesChart) {
            issueTypesChart.remove();
            console.log('Removed Issue Types chart');
        }
        
        // Remove any canvas elements with these IDs
        const statusCanvas = document.getElementById('statusChartCanvas');
        if (statusCanvas) {
            statusCanvas.remove();
            console.log('Removed Status Distribution canvas');
        }
        
        const issueTypesCanvas = document.getElementById('issueTypesChartCanvas');
        if (issueTypesCanvas) {
            issueTypesCanvas.remove();
            console.log('Removed Issue Types canvas');
        }
        
        // Remove any divs containing these chart titles
        const allDivs = document.querySelectorAll('div');
        allDivs.forEach(div => {
            const text = div.textContent || '';
            if (text.includes('Status Distribution') || text.includes('Issue Types')) {
                // Check if this div contains chart-related content
                if (div.querySelector('canvas') || div.querySelector('.chart-container') || div.querySelector('.compact-chart')) {
                    div.remove();
                    console.log('Removed chart section:', text.substring(0, 50));
                }
            }
        });
        
        // Remove any rows containing these charts
        const allRows = document.querySelectorAll('.row');
        allRows.forEach(row => {
            const text = row.textContent || '';
            if (text.includes('Status Distribution') || text.includes('Issue Types')) {
                if (row.querySelector('canvas') || row.querySelector('.chart-container') || row.querySelector('.compact-chart')) {
                    row.remove();
                    console.log('Removed chart row');
                }
            }
        });
    }
    
    // Function to remove chart-related CSS
    function removeChartCSS() {
        // Remove any style elements containing chart-related CSS
        const styleElements = document.querySelectorAll('style');
        styleElements.forEach(style => {
            const css = style.textContent || '';
            if (css.includes('statusChart') || css.includes('issueTypesChart') || 
                css.includes('chart-container') || css.includes('compact-chart')) {
                style.remove();
                console.log('Removed chart-related CSS');
            }
        });
        
        // Remove any link elements pointing to chart CSS files
        const linkElements = document.querySelectorAll('link[href*="chart"]');
        linkElements.forEach(link => {
            link.remove();
            console.log('Removed chart CSS link:', link.href);
        });
    }
    
    // Function to remove chart-related JavaScript
    function removeChartJS() {
        // Remove any script elements containing chart-related code
        const scriptElements = document.querySelectorAll('script');
        scriptElements.forEach(script => {
            const js = script.textContent || '';
            if (js.includes('statusChart') || js.includes('issueTypesChart') || 
                js.includes('Chart.js') || js.includes('chartjs')) {
                script.remove();
                console.log('Removed chart-related JavaScript');
            }
        });
        
        // Remove any script elements pointing to chart JS files
        const chartScripts = document.querySelectorAll('script[src*="chart"]');
        chartScripts.forEach(script => {
            script.remove();
            console.log('Removed chart JavaScript:', script.src);
        });
    }
    
    // Main function to clean up charts
    function cleanupCharts() {
        console.log('Starting chart cleanup...');
        
        // Remove chart sections
        removeChartSections();
        
        // Remove chart CSS
        removeChartCSS();
        
        // Remove chart JavaScript
        removeChartJS();
        
        console.log('Chart cleanup completed!');
    }
    
    // Auto-run when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', cleanupCharts);
    } else {
        cleanupCharts();
    }
    
    // Also run after a delay to catch dynamically loaded content
    setTimeout(cleanupCharts, 2000);
    
    // Export for manual use
    window.removeCharts = cleanupCharts;
    
})();