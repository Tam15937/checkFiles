const { createApp } = Vue;

const FileDamageAnalyzer = createApp({
    data() {
        return {
            originalDir: '/usr/bin',
            damagedDir: '/tmp/damaged_bin',
            isAnalyzing: false,
            taskId: null,
            results: [],
            message: null,
            messageType: 'info'
        };
    },

    computed: {

        stats() {
            const total = this.results.length;
            const damaged = this.results.filter(f => f.damaged).length;
            const good = total - damaged;

            return { total, damaged, good };
        },


        hasDamagedFiles() {
            return this.stats.damaged > 0;
        }
    },

    methods: {

        async startAnalysis() {
            if (this.isAnalyzing) return;

            if (!this.originalDir || !this.damagedDir) {
                this.showMessage('Укажите обе директории', 'error');
                return;
            }

            this.isAnalyzing = true;
            this.taskId = null;
            this.results = [];
            this.showMessage('Запуск анализа...', 'info');

            try {
                const response = await fetch('/api/analyze', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        originalDir: this.originalDir,
                        damagedDir: this.damagedDir
                    })
                });

                if (!response.ok) throw new Error(`HTTP ${response.status}`);

                const data = await response.json();
                this.taskId = data.taskId;
                this.showMessage('Анализ запущен. Получаем результаты...', 'info');


                this.pollResults();

            } catch (error) {
                console.error('Ошибка:', error);
                this.showMessage(`Ошибка: ${error.message}`, 'error');
                this.isAnalyzing = false;
            }
        },


        async pollResults() {
            if (!this.taskId) return;

            try {
                const response = await fetch(`/api/results/${this.taskId}`);

                if (!response.ok) throw new Error(`HTTP ${response.status}`);

                const results = await response.json();

                if (results && results.length > 0) {

                    this.results = results;
                    this.isAnalyzing = false;

                    if (results.length === 0) {
                        this.showMessage('Анализ завершен. Файлы не найдены.', 'info');
                    } else if (this.hasDamagedFiles) {
                        this.showMessage(`Найдено ${this.stats.damaged} поврежденных файлов`, 'success');
                    } else {
                        this.showMessage('Все файлы совпадают. Повреждений не найдено.', 'success');
                    }
                } else {

                    setTimeout(() => this.pollResults(), 2000);
                }

            } catch (error) {
                console.error('Ошибка опроса:', error);
                this.showMessage('Ошибка получения результатов', 'error');
                this.isAnalyzing = false;
            }
        },


        clearFields() {
            this.originalDir = '';
            this.damagedDir = '';
            this.results = [];
            this.message = null;
        },


        showMessage(text, type = 'info') {
            this.message = text;
            this.messageType = type;


            if (type !== 'info') {
                setTimeout(() => {
                    this.message = null;
                }, 5000);
            }
        },


        formatByte(byte) {
            if (typeof byte === 'number') {
                return byte.toString(16).padStart(2, '0').toUpperCase();
            }
            return '??';
        }
    },


    template: `
        <div class="app">
            <!-- Заголовок -->
            <div class="header">
                <h1>🔍 File Damage Analyzer</h1>
                <p>Анализ поврежденных файлов в директориях</p>
            </div>


            <div v-if="message" :class="['message', 'message-' + messageType]">
                {{ message }}
            </div>


            <div class="form">
                <div class="form-group">
                    <label>Оригинальная директория:</label>
                    <input
                        v-model="originalDir"
                        type="text"
                        class="form-control"
                        placeholder="/path/to/original"
                        :disabled="isAnalyzing"
                    >
                </div>

                <div class="form-group">
                    <label>Поврежденная директория:</label>
                    <input
                        v-model="damagedDir"
                        type="text"
                        class="form-control"
                        placeholder="/path/to/damaged"
                        :disabled="isAnalyzing"
                    >
                </div>

                <div>
                    <button
                        @click="startAnalysis"
                        class="btn btn-primary"
                        :disabled="isAnalyzing || !originalDir || !damagedDir"
                    >
                        {{ isAnalyzing ? 'Анализ...' : '🚀 Начать анализ' }}
                    </button>

                    <button
                        @click="clearFields"
                        class="btn btn-secondary"
                        :disabled="isAnalyzing"
                    >
                        Очистить
                    </button>
                </div>
            </div>


            <div v-if="results.length > 0" class="results">
                <div class="results-header">
                    <h3>Результаты анализа</h3>
                    <div v-if="taskId" style="font-size: 12px; color: #7f8c8d;">
                        Task ID: {{ taskId }}
                    </div>
                </div>

                <div class="results-content">

                    <div v-if="results.length > 0" class="stats">
                        <div class="stat-item">
                            <div class="stat-value">{{ stats.total }}</div>
                            <div class="stat-label">Всего файлов</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value" style="color: #27ae60">{{ stats.good }}</div>
                            <div class="stat-label">Исправно</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value" style="color: #e74c3c">{{ stats.damaged }}</div>
                            <div class="stat-label">Повреждено</div>
                        </div>
                    </div>


                    <div v-for="file in results" :key="file.filename" class="result-item">
                        <div class="result-header">
                            <div class="filename">{{ file.filename }}</div>
                            <div :class="['status', file.damaged ? 'status-bad' : 'status-good']">
                                {{ file.damaged ? '🚫 ПОВРЕЖДЕН' : '✅ ИСПРАВЕН' }}
                            </div>
                        </div>


                        <div v-if="file.damaged && file.damages" class="damage-details">
                            <div v-for="(damage, index) in file.damages" :key="index" class="damage-item">
                                Смещение: {{ damage.offset }} |
                                Оригинал:
                                <span class="byte-original">{{ formatByte(damage.originalByte) }}</span> |
                                Поврежден:
                                <span class="byte-damaged">{{ formatByte(damage.damagedByte) }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>


            <div v-if="isAnalyzing" class="loading">
                <div class="spinner"></div>
                <p>Выполняется анализ файлов...</p>
                <p style="font-size: 12px; color: #95a5a6;">Пожалуйста, подождите</p>
            </div>


            <div v-if="!isAnalyzing && results.length === 0 && !message" class="loading">
                <p>Укажите директории и нажмите "Начать анализ"</p>
            </div>
        </div>
    `
});

FileDamageAnalyzer.mount('#app');