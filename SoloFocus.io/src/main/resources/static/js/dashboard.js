// Create clean copies from globally defined backend data
// These ...FromBackend variables are expected to be set by an inline script in the HTML
const contributionData = Object.assign({}, contributionDataFromBackend || {});
const weeklyData = Object.assign({}, weeklyDataFromBackend || {});
const monthlyData = Object.assign({}, monthlyDataFromBackend || {});
const yearlyData = Object.assign({}, yearlyDataFromBackend || {});

console.log('Data loaded:', {
    contribution: Object.keys(contributionData).length,
    weekly: Object.keys(weeklyData).length,
    monthly: Object.keys(monthlyData).length,
    yearly: Object.keys(yearlyData).length
});

let currentPeriod = 'week';
let currentChart = null;
let ctx = null;

function formatDateKey(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

function generateContributionGraph() {
    const container = document.getElementById('contributionGraph');
    if (!container) return;
    
    const today = new Date();
    const currentYear = today.getFullYear();
    
    // Calculate weeks to show (52 weeks)
    const endDate = new Date(today);
    endDate.setHours(0, 0, 0, 0);
    
    const startDate = new Date(endDate);
    startDate.setDate(startDate.getDate() - 364); // 52 weeks = 364 days
    
    // Adjust to start on Sunday
    const startDay = startDate.getDay();
    if (startDay !== 0) {
        startDate.setDate(startDate.getDate() - startDay);
    }
    
    // Build grid data
    const weeks = [];
    let totalContributions = 0;
    let currentDate = new Date(startDate);
    
    while (currentDate <= endDate) {
        const week = [];
        for (let i = 0; i < 7; i++) {
            if (currentDate > endDate) {
                week.push(null);
            } else {
                const dateKey = formatDateKey(currentDate);
                const minutes = contributionData[dateKey] || 0;
                if (minutes > 0) totalContributions++;
                
                let level = 0;
                if (minutes > 0 && minutes < 30) level = 1;
                else if (minutes >= 30 && minutes < 60) level = 2;
                else if (minutes >= 60 && minutes < 120) level = 3;
                else if (minutes >= 120) level = 4;
                
                week.push({
                    date: dateKey,
                    minutes: minutes,
                    level: level,
                    dateObj: new Date(currentDate)
                });
            }
            currentDate.setDate(currentDate.getDate() + 1);
        }
        weeks.push(week);
    }
    
    // Update contribution count
    document.getElementById('contributionCount').textContent = totalContributions;
    document.getElementById('contributionYear').textContent = currentYear;
    
    // Generate HTML
    let html = '<div class="github-graph">';
    
    // Month labels
    html += '<div class="graph-months">';
    let lastMonth = -1;
    weeks.forEach((week, weekIdx) => {
        const firstDay = week.find(d => d !== null);
        if (firstDay) {
            const month = firstDay.dateObj.getMonth();
            if (month !== lastMonth && weekIdx > 0) {
                const monthName = firstDay.dateObj.toLocaleString('default', { month: 'short' });
                html += `<div class="graph-month" style="grid-column: ${weekIdx + 2};">${monthName}</div>`;
                lastMonth = month;
            }
        }
    });
    html += '</div>';
    
    // Day labels + grid
    html += '<div class="graph-body">';
    html += '<div class="graph-days">';
    html += '<div class="graph-day-label" style="grid-row: 2;">Mon</div>';
    html += '<div class="graph-day-label" style="grid-row: 4;">Wed</div>';
    html += '<div class="graph-day-label" style="grid-row: 6;">Fri</div>';
    html += '</div>';
    
    // Contribution squares
    html += '<div class="graph-grid">';
    weeks.forEach(week => {
        html += '<div class="graph-week">';
        week.forEach(day => {
            if (day === null) {
                html += '<div class="graph-square graph-empty"></div>';
            } else {
                html += `<div class="graph-square graph-level-${day.level}" data-date="${day.date}" data-minutes="${day.minutes}" data-level="${day.level}"></div>`;
            }
        });
        html += '</div>';
    });
    html += '</div></div></div>';
    
    container.innerHTML = html;
    
    // Add tooltips
    setTimeout(() => {
        document.querySelectorAll('.graph-square:not(.graph-empty)').forEach(square => {
            square.addEventListener('mouseenter', function(e) {
                const date = new Date(this.dataset.date + 'T00:00:00');
                const minutes = parseInt(this.dataset.minutes) || 0;
                const dateStr = date.toLocaleDateString('en-US', {
                    weekday: 'long',
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                });
                
                let message = 'No focus time';
                if (minutes > 0) {
                    if (minutes < 60) {
                        message = `${minutes} minute${minutes !== 1 ? 's' : ''}`;
                    } else {
                        const h = Math.floor(minutes / 60);
                        const m = minutes % 60;
                        message = m > 0 ? `${h}h ${m}m` : `${h} hour${h !== 1 ? 's' : ''}`;
                    }
                }
                
                const tooltip = document.createElement('div');
                tooltip.className = 'graph-tooltip';
                tooltip.innerHTML = `<strong>${message}</strong> on ${dateStr}`;
                document.body.appendChild(tooltip);
                
                const rect = this.getBoundingClientRect();
                const left = Math.max(10, Math.min(
                    rect.left + rect.width / 2 - tooltip.offsetWidth / 2,
                    window.innerWidth - tooltip.offsetWidth - 10
                ));
                
                tooltip.style.left = left + 'px';
                tooltip.style.top = (rect.top - tooltip.offsetHeight - 8) + 'px';
            });
            
            square.addEventListener('mouseleave', function() {
                const tooltip = document.querySelector('.graph-tooltip');
                if (tooltip) tooltip.remove();
            });
        });
    }, 50);
}

Chart.defaults.color = '#cbd5e1';
Chart.defaults.borderColor = 'rgba(148, 163, 184, 0.2)';

function prepareChartData(period) {
    let data, labels, description;

    if (period === 'week') {
        const today = new Date();
        const sortedKeys = [];
        labels = [];
        
        for (let i = 6; i >= 0; i--) {
            const date = new Date(today);
            date.setDate(date.getDate() - i);
            sortedKeys.push(formatDateKey(date));
            labels.push(date.toLocaleDateString('en-US', { weekday: 'short' }));
        }
        
        const minutes = sortedKeys.map(key => Math.round(weeklyData[key] || 0));
        description = 'Focus minutes per day this week';
        return { labels, minutes, description };
        
    } else if (period === 'month') {
        const sortedKeys = Object.keys(monthlyData).sort();
        labels = sortedKeys.map(date => {
            const d = new Date(date + 'T00:00:00');
            return d.toLocaleDateString('en-US', { day: 'numeric', month: 'short' });
        });
        const minutes = sortedKeys.map(key => Math.round(monthlyData[key] || 0));
        description = 'Focus minutes per day this month';
        return { labels, minutes, description };
        
    } else {
        const sortedKeys = Object.keys(yearlyData).sort();
        labels = sortedKeys.map(month => {
            const [year, monthNum] = month.split('-');
            const d = new Date(parseInt(year), parseInt(monthNum) - 1, 1);
            return d.toLocaleDateString('en-US', { month: 'short', year: '2-digit' });
        });
        const minutes = sortedKeys.map(key => Math.round(yearlyData[key] || 0));
        description = 'Focus minutes per month this year';
        return { labels, minutes, description };
    }
}

function updateChart(period) {
    const { labels, minutes, description } = prepareChartData(period);
    
    const chartSection = document.querySelector('.chart-section .section-description');
    if (chartSection) chartSection.textContent = description;
    
    let maxMinutes = Math.max(...minutes.filter(m => m > 0), 60);
    const maxHours = Math.ceil(maxMinutes / 60);
    
    let stepHours = maxHours <= 2 ? 0.5 : maxHours <= 6 ? 1 : maxHours <= 12 ? 2 : maxHours <= 24 ? 3 : 6;
    const stepMinutes = stepHours * 60;
    const maxYValue = Math.ceil(maxHours / stepHours) * stepHours * 60;
    
    if (!ctx) {
        const canvas = document.getElementById('weeklyChart');
        if (!canvas) return;
        ctx = canvas.getContext('2d');
    }
    
    if (currentChart) {
        currentChart.destroy();
    }
    
    const backgroundColors = minutes.map(m => {
        if (m === 0) return 'rgba(136, 136, 136, 0.2)';
        if (m < 30) return 'rgba(16, 185, 129, 0.4)';
        if (m < 60) return 'rgba(16, 185, 129, 0.6)';
        if (m < 120) return 'rgba(16, 185, 129, 0.8)';
        return 'rgba(239, 68, 68, 0.8)';
    });

    const borderColors = minutes.map(m => {
        if (m === 0) return 'rgba(136, 136, 136, 0.3)';
        if (m < 30) return 'rgba(16, 185, 129, 0.6)';
        if (m < 60) return 'rgba(16, 185, 129, 0.8)';
        if (m < 120) return 'rgba(16, 185, 129, 1)';
        return 'rgba(239, 68, 68, 1)';
    });

    currentChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Focus Minutes',
                data: minutes,
                backgroundColor: backgroundColors,
                borderColor: borderColors,
                borderWidth: 2,
                borderRadius: 12,
                borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: 'rgba(30, 41, 59, 0.95)',
                    padding: 14,
                    titleFont: { size: 14, weight: '600' },
                    bodyFont: { size: 13 },
                    titleColor: '#34d399',
                    bodyColor: '#f0f0f0',
                    borderColor: '#1a1a1a',
                    borderWidth: 1,
                    cornerRadius: 8,
                    callbacks: {
                        label: function(context) {
                            const m = context.parsed.y;
                            if (m === 0) return '😴 No focus time';
                            if (m < 30) return `🌱 ${m}m - Getting started!`;
                            if (m < 60) return `🌿 ${m}m - Good progress!`;
                            const h = Math.floor(m / 60);
                            const mins = m % 60;
                            const time = mins > 0 ? `${h}h ${mins}m` : `${h}h`;
                            return m < 120 ? `🌳 ${time} - Great work!` : `🔥 ${time} - On fire!`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max: maxYValue || 60,
                    grid: { color: 'rgba(136, 136, 136, 0.15)', drawBorder: false },
                    ticks: {
                        stepSize: stepMinutes,
                        font: { size: 12, weight: '500' },
                        color: '#d4d4d4',
                        padding: 10,
                        callback: function(value) {
                            if (value === 0) return '0';
                            const hours = value / 60;
                            return hours >= 10 ? Math.round(hours) + 'h' : 
                                   hours === Math.floor(hours) ? hours + 'h' : 
                                   hours.toFixed(1) + 'h';
                        }
                    }
                },
                x: {
                    grid: { display: false },
                    ticks: { font: { size: 13, weight: '500' }, color: '#d4d4d4' }
                }
            }
        }
    });
}

function init() {
    if (typeof Chart === 'undefined') {
        console.error('Chart.js not loaded');
        return;
    }
    
    generateContributionGraph();
    updateChart('week');
    
    const btn = document.getElementById('timeFilterBtn');
    const dropdown = document.getElementById('timeFilterDropdown');
    const text = document.getElementById('timeFilterText');
    
    if (btn && dropdown) {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            dropdown.classList.toggle('show');
        });
        
        document.addEventListener('click', (e) => {
            if (!btn.contains(e.target) && !dropdown.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });
        
        document.querySelectorAll('.time-filter-item').forEach(item => {
            item.addEventListener('click', function() {
                const period = this.dataset.period;
                document.querySelectorAll('.time-filter-item').forEach(i => i.classList.remove('active'));
                this.classList.add('active');
                if (text) text.textContent = this.textContent;
                updateChart(period);
                dropdown.classList.remove('show');
            });
        });
    }
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
} else {
    init();
}