// Modern CSS-based Charts - No Canvas Issues
class ModernCharts {
    constructor() {
        this.charts = {};
        this.colors = {
            open: '#34C759',
            'in-progress': '#FF9500',
            resolved: '#007AFF',
            closed: '#8E8E93',
            reopened: '#FF3B30',
            bug: '#FF3B30',
            story: '#34C759',
            task: '#007AFF',
            epic: '#FF9500',
            subtask: '#8E8E93'
        };
    }

    // Create Status Distribution Chart
    createStatusChart(containerId, data) {
        const container = document.getElementById(containerId);
        if (!container) return;

        container.innerHTML = `
            <div class="chart-section">
                <h3 class="chart-title">
                    <i class="fas fa-chart-bar"></i>
                    Status Distribution
                </h3>
                <div class="status-chart" id="statusChartContent">
                    ${this.generateStatusChartHTML(data)}
                </div>
            </div>
        `;

        // Animate the bars after a short delay
        setTimeout(() => {
            this.animateStatusBars();
        }, 500);
    }

    generateStatusChartHTML(data) {
        if (!data || data.length === 0) {
            return `
                <div class="chart-empty">
                    <i class="fas fa-chart-bar"></i>
                    <p>No status data available</p>
                </div>
            `;
        }

        const total = data.reduce((sum, item) => sum + item.count, 0);
        
        return data.map(item => {
            const percentage = total > 0 ? (item.count / total) * 100 : 0;
            const statusClass = item.status.toLowerCase().replace(/\s+/g, '-');
            
            return `
                <div class="status-item">
                    <div class="status-label">${item.status}</div>
                    <div class="status-bar">
                        <div class="status-fill status-${statusClass}" 
                             style="width: 0%" 
                             data-width="${percentage}%">
                        </div>
                    </div>
                    <div class="status-count">${item.count}</div>
                </div>
            `;
        }).join('');
    }

    animateStatusBars() {
        const bars = document.querySelectorAll('.status-fill');
        bars.forEach(bar => {
            const targetWidth = bar.getAttribute('data-width');
            setTimeout(() => {
                bar.style.width = targetWidth;
            }, 100);
        });
    }

    // Create Issue Types Chart
    createIssueTypesChart(containerId, data) {
        const container = document.getElementById(containerId);
        if (!container) return;

        container.innerHTML = `
            <div class="chart-section">
                <h3 class="chart-title">
                    <i class="fas fa-chart-pie"></i>
                    Issue Types
                </h3>
                <div class="issue-types-chart" id="issueTypesChartContent">
                    ${this.generateIssueTypesChartHTML(data)}
                </div>
            </div>
        `;

        // Animate the progress bars
        setTimeout(() => {
            this.animateIssueProgressBars();
        }, 500);
    }

    generateIssueTypesChartHTML(data) {
        if (!data || data.length === 0) {
            return `
                <div class="chart-empty">
                    <i class="fas fa-chart-pie"></i>
                    <p>No issue types data available</p>
                </div>
            `;
        }

        const total = data.reduce((sum, item) => sum + item.count, 0);
        
        return data.map(item => {
            const percentage = total > 0 ? (item.count / total) * 100 : 0;
            const typeClass = item.type.toLowerCase();
            const icon = this.getIssueTypeIcon(item.type);
            
            return `
                <div class="issue-type-card issue-${typeClass}">
                    <div class="issue-type-icon">${icon}</div>
                    <div class="issue-type-name">${item.type}</div>
                    <div class="issue-type-count">${item.count}</div>
                    <div class="issue-type-percentage">${percentage.toFixed(1)}%</div>
                    <div class="issue-progress">
                        <div class="issue-progress-fill status-${typeClass}" 
                             style="width: 0%" 
                             data-width="${percentage}%">
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    }

    getIssueTypeIcon(type) {
        const icons = {
            'Bug': '<i class="fas fa-bug"></i>',
            'Story': '<i class="fas fa-book"></i>',
            'Task': '<i class="fas fa-tasks"></i>',
            'Epic': '<i class="fas fa-flag"></i>',
            'Subtask': '<i class="fas fa-list-ul"></i>'
        };
        return icons[type] || '<i class="fas fa-question"></i>';
    }

    animateIssueProgressBars() {
        const progressBars = document.querySelectorAll('.issue-progress-fill');
        progressBars.forEach(bar => {
            const targetWidth = bar.getAttribute('data-width');
            setTimeout(() => {
                bar.style.width = targetWidth;
            }, 100);
        });
    }

    // Update chart data
    updateStatusChart(containerId, newData) {
        const content = document.getElementById('statusChartContent');
        if (content) {
            content.innerHTML = this.generateStatusChartHTML(newData);
            setTimeout(() => {
                this.animateStatusBars();
            }, 100);
        }
    }

    updateIssueTypesChart(containerId, newData) {
        const content = document.getElementById('issueTypesChartContent');
        if (content) {
            content.innerHTML = this.generateIssueTypesChartHTML(newData);
            setTimeout(() => {
                this.animateIssueProgressBars();
            }, 100);
        }
    }

    // Show loading state
    showLoading(containerId, message = 'Loading chart data...') {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = `
                <div class="chart-section">
                    <div class="chart-loading">
                        <i class="fas fa-spinner"></i>
                        ${message}
                    </div>
                </div>
            `;
        }
    }

    // Show error state
    showError(containerId, message = 'Error loading chart data') {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = `
                <div class="chart-section">
                    <div class="chart-empty">
                        <i class="fas fa-exclamation-triangle"></i>
                        <p>${message}</p>
                    </div>
                </div>
            `;
        }
    }

    // Sample data generator for testing
    generateSampleData() {
        return {
            statusData: [
                { status: 'Open', count: 15 },
                { status: 'In Progress', count: 8 },
                { status: 'Resolved', count: 12 },
                { status: 'Closed', count: 25 },
                { status: 'Reopened', count: 3 }
            ],
            issueTypesData: [
                { type: 'Bug', count: 20 },
                { type: 'Story', count: 15 },
                { type: 'Task', count: 10 },
                { type: 'Epic', count: 3 },
                { type: 'Subtask', count: 7 }
            ]
        };
    }
}

// Initialize global charts instance
window.modernCharts = new ModernCharts();

// Auto-initialize if data is available
document.addEventListener('DOMContentLoaded', function() {
    // Check if we have chart containers and initialize with sample data
    const statusContainer = document.getElementById('statusChart');
    const issueTypesContainer = document.getElementById('issueTypesChart');
    
    if (statusContainer && !statusContainer.innerHTML.trim()) {
        const sampleData = window.modernCharts.generateSampleData();
        window.modernCharts.createStatusChart('statusChart', sampleData.statusData);
    }
    
    if (issueTypesContainer && !issueTypesContainer.innerHTML.trim()) {
        const sampleData = window.modernCharts.generateSampleData();
        window.modernCharts.createIssueTypesChart('issueTypesChart', sampleData.issueTypesData);
    }
});