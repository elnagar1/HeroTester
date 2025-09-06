// Canvas Charts Fix - Universal Solution
// This script will fix any canvas charts that are growing too large

(function() {
    'use strict';
    
    // Configuration
    const CONFIG = {
        maxHeight: 350,
        maxWidth: 800,
        checkInterval: 100, // Check every 100ms
        animationDuration: 300
    };
    
    // Store original canvas dimensions
    const originalDimensions = new Map();
    
    // Fix function
    function fixCanvas(canvas) {
        if (!canvas || canvas.tagName !== 'CANVAS') return;
        
        // Store original dimensions if not already stored
        if (!originalDimensions.has(canvas)) {
            originalDimensions.set(canvas, {
                width: canvas.width,
                height: canvas.height
            });
        }
        
        // Force canvas to reasonable dimensions
        if (canvas.height > CONFIG.maxHeight) {
            canvas.height = CONFIG.maxHeight;
            canvas.style.height = CONFIG.maxHeight + 'px';
        }
        
        if (canvas.width > CONFIG.maxWidth) {
            canvas.width = CONFIG.maxWidth;
            canvas.style.width = CONFIG.maxWidth + 'px';
        }
        
        // Apply CSS fixes
        canvas.style.maxHeight = CONFIG.maxHeight + 'px';
        canvas.style.maxWidth = '100%';
        canvas.style.overflow = 'hidden';
        canvas.style.display = 'block';
        
        // Override canvas properties to prevent future resizing
        if (!canvas._fixed) {
            Object.defineProperty(canvas, 'width', {
                get: function() { return Math.min(this._width || 800, CONFIG.maxWidth); },
                set: function(value) { 
                    this._width = Math.min(value, CONFIG.maxWidth);
                },
                configurable: true
            });
            
            Object.defineProperty(canvas, 'height', {
                get: function() { return Math.min(this._height || 350, CONFIG.maxHeight); },
                set: function(value) { 
                    this._height = Math.min(value, CONFIG.maxHeight);
                },
                configurable: true
            });
            
            canvas._fixed = true;
        }
    }
    
    // Fix all canvases
    function fixAllCanvases() {
        const canvases = document.querySelectorAll('canvas');
        canvases.forEach(fixCanvas);
        
        // Also fix specific problematic charts
        const statusChart = document.getElementById('statusChart');
        const issueTypesChart = document.getElementById('issueTypesChart');
        const statusCanvas = document.getElementById('statusChartCanvas');
        const issueTypesCanvas = document.getElementById('issueTypesChartCanvas');
        
        [statusChart, issueTypesChart, statusCanvas, issueTypesCanvas].forEach(element => {
            if (element) {
                if (element.tagName === 'CANVAS') {
                    fixCanvas(element);
                } else {
                    // Fix container
                    element.style.maxHeight = CONFIG.maxHeight + 'px';
                    element.style.overflow = 'hidden';
                    element.style.position = 'relative';
                }
            }
        });
    }
    
    // Replace canvas with modern chart
    function replaceCanvasWithModernChart(canvasId, chartType, data) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;
        
        const container = canvas.parentElement;
        if (!container) return;
        
        // Create modern chart container
        const modernChart = document.createElement('div');
        modernChart.className = 'modern-chart-container';
        modernChart.style.cssText = `
            max-height: ${CONFIG.maxHeight}px;
            overflow: hidden;
            background: #1a2a47;
            border-radius: 12px;
            padding: 20px;
            margin: 20px 0;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
        `;
        
        if (chartType === 'status') {
            modernChart.innerHTML = generateStatusChartHTML(data);
        } else if (chartType === 'issueTypes') {
            modernChart.innerHTML = generateIssueTypesChartHTML(data);
        }
        
        // Replace canvas with modern chart
        container.replaceChild(modernChart, canvas);
    }
    
    // Generate Status Chart HTML
    function generateStatusChartHTML(data) {
        if (!data || data.length === 0) {
            return `
                <h3 style="color: #e0f7fa; margin-bottom: 20px;">
                    <i class="fas fa-chart-bar" style="color: #3ecbf7;"></i>
                    Status Distribution
                </h3>
                <div style="text-align: center; color: #8E8E93; padding: 40px;">
                    <i class="fas fa-chart-bar" style="font-size: 3rem; margin-bottom: 15px; color: #A2A2A2;"></i>
                    <p>No status data available</p>
                </div>
            `;
        }
        
        const total = data.reduce((sum, item) => sum + item.count, 0);
        
        return `
            <h3 style="color: #e0f7fa; margin-bottom: 20px;">
                <i class="fas fa-chart-bar" style="color: #3ecbf7;"></i>
                Status Distribution
            </h3>
            <div style="display: flex; flex-direction: column; gap: 15px;">
                ${data.map(item => {
                    const percentage = total > 0 ? (item.count / total) * 100 : 0;
                    const statusClass = item.status.toLowerCase().replace(/\s+/g, '-');
                    
                    return `
                        <div style="display: flex; align-items: center; background: #0e2340; border-radius: 8px; padding: 12px 15px; border-left: 4px solid #3ecbf7;">
                            <div style="color: #e0f7fa; font-weight: 500; min-width: 120px; margin-right: 15px;">${item.status}</div>
                            <div style="flex: 1; height: 20px; background: #0a1833; border-radius: 10px; overflow: hidden; position: relative; margin-right: 15px;">
                                <div style="height: 100%; border-radius: 10px; width: ${percentage}%; background: linear-gradient(90deg, #3ecbf7, #1e90ff); transition: width 0.8s ease;"></div>
                            </div>
                            <div style="color: #3ecbf7; font-weight: 600; min-width: 50px; text-align: right;">${item.count}</div>
                        </div>
                    `;
                }).join('')}
            </div>
        `;
    }
    
    // Generate Issue Types Chart HTML
    function generateIssueTypesChartHTML(data) {
        if (!data || data.length === 0) {
            return `
                <h3 style="color: #e0f7fa; margin-bottom: 20px;">
                    <i class="fas fa-chart-pie" style="color: #3ecbf7;"></i>
                    Issue Types
                </h3>
                <div style="text-align: center; color: #8E8E93; padding: 40px;">
                    <i class="fas fa-chart-pie" style="font-size: 3rem; margin-bottom: 15px; color: #A2A2A2;"></i>
                    <p>No issue types data available</p>
                </div>
            `;
        }
        
        const total = data.reduce((sum, item) => sum + item.count, 0);
        
        return `
            <h3 style="color: #e0f7fa; margin-bottom: 20px;">
                <i class="fas fa-chart-pie" style="color: #3ecbf7;"></i>
                Issue Types
            </h3>
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px;">
                ${data.map(item => {
                    const percentage = total > 0 ? (item.count / total) * 100 : 0;
                    const typeClass = item.type.toLowerCase();
                    const icon = getIssueTypeIcon(item.type);
                    
                    return `
                        <div style="background: #0e2340; border-radius: 8px; padding: 15px; text-align: center; border: 2px solid transparent; transition: all 0.3s ease; position: relative; overflow: hidden;">
                            <div style="position: absolute; top: 0; left: 0; right: 0; height: 4px; background: linear-gradient(90deg, #3ecbf7, #1e90ff);"></div>
                            <div style="font-size: 2rem; margin-bottom: 10px; color: #3ecbf7;">${icon}</div>
                            <div style="color: #e0f7fa; font-weight: 600; margin-bottom: 8px;">${item.type}</div>
                            <div style="color: #3ecbf7; font-size: 1.5rem; font-weight: 700;">${item.count}</div>
                            <div style="color: #b0c4de; font-size: 0.9rem; margin-top: 5px;">${percentage.toFixed(1)}%</div>
                            <div style="margin-top: 10px; height: 6px; background: #0a1833; border-radius: 3px; overflow: hidden;">
                                <div style="height: 100%; border-radius: 3px; width: ${percentage}%; background: linear-gradient(90deg, #3ecbf7, #1e90ff); transition: width 1s ease;"></div>
                            </div>
                        </div>
                    `;
                }).join('')}
            </div>
        `;
    }
    
    // Get issue type icon
    function getIssueTypeIcon(type) {
        const icons = {
            'Bug': '<i class="fas fa-bug"></i>',
            'Story': '<i class="fas fa-book"></i>',
            'Task': '<i class="fas fa-tasks"></i>',
            'Epic': '<i class="fas fa-flag"></i>',
            'Subtask': '<i class="fas fa-list-ul"></i>'
        };
        return icons[type] || '<i class="fas fa-question"></i>';
    }
    
    // Mutation Observer to catch new canvases
    function startMonitoring() {
        const observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                if (mutation.type === 'childList') {
                    mutation.addedNodes.forEach(function(node) {
                        if (node.nodeType === 1) { // Element node
                            if (node.tagName === 'CANVAS') {
                                fixCanvas(node);
                            } else {
                                const canvases = node.querySelectorAll ? node.querySelectorAll('canvas') : [];
                                canvases.forEach(fixCanvas);
                            }
                        }
                    });
                } else if (mutation.type === 'attributes' && 
                          (mutation.attributeName === 'width' || mutation.attributeName === 'height' || mutation.attributeName === 'style')) {
                    const target = mutation.target;
                    if (target.tagName === 'CANVAS') {
                        fixCanvas(target);
                    }
                }
            });
        });
        
        observer.observe(document.body, {
            childList: true,
            subtree: true,
            attributes: true,
            attributeFilter: ['width', 'height', 'style']
        });
    }
    
    // Periodic check
    function startPeriodicCheck() {
        setInterval(fixAllCanvases, CONFIG.checkInterval);
    }
    
    // Initialize when DOM is ready
    function init() {
        // Fix existing canvases
        fixAllCanvases();
        
        // Start monitoring
        startMonitoring();
        startPeriodicCheck();
        
        // Override Chart.js if present
        if (typeof Chart !== 'undefined') {
            const originalResize = Chart.prototype.resize;
            Chart.prototype.resize = function() {
                if (this.canvas.height > CONFIG.maxHeight) {
                    this.canvas.height = CONFIG.maxHeight;
                }
                if (this.canvas.width > CONFIG.maxWidth) {
                    this.canvas.width = CONFIG.maxWidth;
                }
                return originalResize.call(this);
            };
        }
        
        console.log('Canvas Charts Fix initialized');
    }
    
    // Auto-initialize
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
    
    // Export for manual use
    window.CanvasFix = {
        fixAll: fixAllCanvases,
        fixCanvas: fixCanvas,
        replaceWithModernChart: replaceCanvasWithModernChart,
        config: CONFIG
    };
    
})();