<template>
  <div class="min-h-screen flex flex-col">
    <!-- Top Bar -->
    <header class="bg-white shadow-sm p-4 flex justify-between items-center sticky top-0 z-10">
      <button @click="urlPush" class="flex items-center text-gray-700 hover:text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 rounded-md p-2">
        <svg class="w-5 h-5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg>
        <span class="font-medium">返回</span>
      </button>

      <div class="flex items-center space-x-3">
        <!-- Dynamic Action Buttons -->
        <div id="actionButtonsContainer" class="flex space-x-2">
          <!-- The delete button is now visible on both original and search tabs -->
          <button
            v-if="currentTab === 'original' || currentTab === 'search'"
            @click="handleBatchAction('delete')"
            class="bg-red-500 hover:bg-red-600 text-white font-semibold py-2 px-4 rounded-lg shadow-md transition duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-opacity-75"
          >
            删除
          </button>
          <button
            v-if="currentTab === 'toDelete'"
            @click="handleBatchAction('undoDelete')"
            class="bg-green-500 hover:bg-green-600 text-white font-semibold py-2 px-4 rounded-lg shadow-md transition duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-opacity-75"
          >
            撤销删除
          </button>
          <button
            v-if="currentTab === 'toModify'"
            @click="handleBatchAction('undoModify')"
            class="bg-green-500 hover:bg-green-600 text-white font-semibold py-2 px-4 rounded-lg shadow-md transition duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-opacity-75"
          >
            撤销修改
          </button>
        </div>

        <!-- Pagination Controls -->
        <div v-if="currentTotalPages > 1" class="flex items-center space-x-2">
          <button
            @click="prevPage"
            :disabled="currentPage === 1"
            class="bg-gray-200 hover:bg-gray-300 text-gray-700 font-semibold py-1 px-3 rounded-lg shadow-sm transition duration-200 ease-in-out disabled:opacity-50 disabled:cursor-not-allowed"
          >
            上一页
          </button>
          <span class="text-gray-700 text-sm">
            {{ currentPage }} / {{ currentTotalPages }}
          </span>
          <button
            @click="nextPage"
            :disabled="currentPage === currentTotalPages"
            class="bg-gray-200 hover:bg-gray-300 text-gray-700 font-semibold py-1 px-3 rounded-lg shadow-sm transition duration-200 ease-in-out disabled:opacity-50 disabled:cursor-not-allowed"
          >
            下一页
          </button>
        </div>
        
        <!-- Add New Term Button, now opens a modal -->
        <button
          v-if="currentTab === 'original'"
          @click="handleAddTerm"
          class="bg-green-500 hover:bg-green-600 text-white font-semibold py-2 px-4 rounded-lg shadow-md transition duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-opacity-75"
        >
          添加术语
        </button>

        <button :disabled="clickButton" @click="handleSync" class="bg-blue-500 hover:bg-blue-600 text-white font-semibold py-2 px-4 rounded-lg shadow-md transition duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-75">
          同步
        </button>
      </div>
    </header>

    <main class="flex-1 p-4 sm:p-6 lg:p-8">
      <!-- Tabs -->
      <div class="bg-white rounded-xl shadow-md overflow-hidden mb-6">
        <div class="flex border-b border-gray-200">
          <button
            @click="switchTab('original')"
            :data-active="currentTab === 'original'"
            class="flex-1 py-3 px-4 text-center font-semibold text-gray-600 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-150 ease-in-out rounded-tl-xl data-[active=true]:text-blue-600 data-[active=true]:border-b-2 data-[active=true]:border-blue-600"
          >
            原生术语
          </button>
          <button
            @click="switchTab('toDelete')"
            :data-active="currentTab === 'toDelete'"
            class="flex-1 py-3 px-4 text-center font-semibold text-gray-600 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-150 ease-in-out data-[active=true]:text-blue-600 data-[active=true]:border-b-2 data-[active=true]:border-blue-600"
          >
            删除待同步
          </button>
          <button
            @click="switchTab('toModify')"
            :data-active="currentTab === 'toModify'"
            class="flex-1 py-3 px-4 text-center font-semibold text-gray-600 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-150 ease-in-out rounded-tr-xl data-[active=true]:text-blue-600 data-[active=true]:border-b-2 data-[active=true]:border-blue-600"
          >
            修改待同步
          </button>
          <button
            @click="switchTab('search')"
            :data-active="currentTab === 'search'"
            class="flex-1 py-3 px-4 text-center font-semibold text-gray-600 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-150 ease-in-out data-[active=true]:text-blue-600 data-[active=true]:border-b-2 data-[active=true]:border-blue-600"
          >
            搜索
          </button>
        </div>
      </div>

      <!-- Search Input for the Search Tab -->
      <div v-if="currentTab === 'search'" class="mb-6 flex items-center space-x-4">
        <input
          v-model="searchKeyword"
          @keydown.enter="searchTerms(1)"
          type="text"
          placeholder="请输入关键词..."
          class="flex-1 px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-150 ease-in-out"
        />
        <button
          @click="searchTerms(1)"
          class="bg-blue-500 hover:bg-blue-600 text-white font-semibold py-2 px-4 rounded-lg shadow-md transition duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-75"
        >
          搜索
        </button>
      </div>

      <!-- Tables Container -->
      <div id="tablesContainer" class="bg-white rounded-xl shadow-md p-4 sm:p-6 overflow-hidden">
        <!-- Table for Original Terms -->
        <div v-show="currentTab === 'original'" class="table-container overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
            <tr>
              <th scope="col" style="width: 5%;" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider rounded-tl-lg">
                <input type="checkbox" v-model="selectAllOriginal" class="form-checkbox h-4 w-4 text-blue-600 rounded focus:ring-blue-500">
              </th>
              <!-- The "原词" (sourceName) column is now editable -->
              <th scope="col" style="width: 45%;" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                原词(点击修改)
              </th>
              <th scope="col" style="width: 50%;" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider rounded-tr-lg">
                译词(点击修改)
              </th>
            </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
            <tr v-if="paginatedOriginalTerms.length === 0">
              <td colspan="3" class="px-4 py-4 text-center text-gray-500">暂无数据</td>
            </tr>
            <tr v-for="term in paginatedOriginalTerms" :key="term.id" class="hover:bg-gray-50 transition-colors duration-150">
              <!-- We now need to check the statue to display the term in the correct tab, even after changes -->
              <template v-if="term.statue === 0">
                <td class="px-4 py-3 table-cell-content">
                  <input type="checkbox" :value="term.id" v-model="selectedOriginalIds" class="term-checkbox h-4 w-4 text-blue-600 rounded focus:ring-blue-500">
                </td>
                <td class="px-4 py-3 text-sm table-cell-content">
                  <div
                    contenteditable="true"
                    @blur="handleInlineEdit(term.id, 'sourceName', $event)"
                    @keydown.enter="handleInlineEditKeydown($event)"
                    class="editable-cell text-gray-900"
                  >
                    {{ term.sourceName }}
                  </div>
                </td>
                <td class="px-4 py-3 text-sm table-cell-content">
                  <div
                    contenteditable="true"
                    @blur="handleInlineEdit(term.id, 'targetName', $event)"
                    @keydown.enter="handleInlineEditKeydown($event)"
                    class="editable-cell text-gray-900"
                  >
                    {{ term.targetName }}
                  </div>
                </td>
              </template>
            </tr>
            </tbody>
          </table>
        </div>

        <!-- Table for Terms to Delete -->
        <div v-show="currentTab === 'toDelete'" class="table-container overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
            <tr>
              <th scope="col" style="width: 5%;" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider rounded-tl-lg">
                <input type="checkbox" v-model="selectAllToDelete" class="form-checkbox h-4 w-4 text-blue-600 rounded focus:ring-blue-500">
              </th>
              <th scope="col" style="width: 45%;" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                原词
              </th>
              <th scope="col" style="width: 50%;" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider rounded-tr-lg">
                译词
              </th>
            </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
            <tr v-if="paginatedToDeleteTerms.length === 0">
              <td colspan="3" class="px-4 py-4 text-center text-gray-500">暂无数据</td>
            </tr>
            <tr v-for="term in paginatedToDeleteTerms" :key="term.id" class="hover:bg-gray-50 transition-colors duration-150">
              <td class="px-4 py-3 table-cell-content">
                <input type="checkbox" :value="term.id" v-model="selectedToDeleteIds" class="term-checkbox h-4 w-4 text-blue-600 rounded focus:ring-blue-500">
              </td>
              <td class="px-4 py-3 text-sm text-gray-900 table-cell-content">{{ term.sourceName }}</td>
              <td class="px-4 py-3 text-sm text-gray-900 table-cell-content">{{ term.targetName }}</td>
            </tr>
            </tbody>
          </table>
        </div>

        <!-- Table for Terms to Modify -->
        <div v-show="currentTab === 'toModify'" class="table-container overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
            <tr>
              <th scope="col" style="width: 5%;" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider rounded-tl-lg">
                <input type="checkbox" v-model="selectAllToModify" class="form-checkbox h-4 w-4 text-blue-600 rounded focus:ring-blue-500">
              </th>
              <th scope="col" style="width: 30%;" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                原词
              </th>
              <th scope="col" style="width: 30%;" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                译词 (原)
              </th>
              <th scope="col" style="width: 35%;" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider rounded-tr-lg">
                译词 (修改后)
              </th>
            </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
            <tr v-if="paginatedToModifyTerms.length === 0">
              <td colspan="4" class="px-4 py-4 text-center text-gray-500">暂无数据</td>
            </tr>
            <tr v-for="term in paginatedToModifyTerms" :key="term.id" class="hover:bg-gray-50 transition-colors duration-150">
              <td class="px-4 py-3 table-cell-content">
                <input type="checkbox" :value="term.id" v-model="selectedToModifyIds" class="term-checkbox h-4 w-4 text-blue-600 rounded focus:ring-blue-500">
              </td>
              <td class="px-4 py-3 text-sm text-gray-900 table-cell-content">{{ term.sourceName }}</td>
              <td class="px-4 py-3 text-sm text-gray-900 table-cell-content">{{ term.targetName }}</td>
              <td class="px-4 py-3 text-sm text-gray-900 table-cell-content">{{ term.modifyName }}</td>
            </tr>
            </tbody>
          </table>
        </div>

        <!-- Table for Search Results -->
        <div v-show="currentTab === 'search'" class="table-container overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
            <tr>
              <th scope="col" style="width: 5%;" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider rounded-tl-lg">
                <!-- Select all checkbox for search results -->
                <input type="checkbox" v-model="selectAllSearch" class="form-checkbox h-4 w-4 text-blue-600 rounded focus:ring-blue-500">
              </th>
              <!-- The "原词" (sourceName) column is now editable in search results -->
              <th scope="col" style="width: 45%;" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                原词(点击修改)
              </th>
              <th scope="col" style="width: 50%;" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider rounded-tr-lg">
                译词(点击修改)
              </th>
            </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
            <tr v-if="searchResults.length === 0">
              <td colspan="3" class="px-4 py-4 text-center text-gray-500">暂无数据</td>
            </tr>
            <tr v-for="term in searchResults" :key="term.id" class="hover:bg-gray-50 transition-colors duration-150">
              <td class="px-4 py-3 table-cell-content">
                <input type="checkbox" :value="term.id" v-model="selectedSearchIds" class="term-checkbox h-4 w-4 text-blue-600 rounded focus:ring-blue-500">
              </td>
              <td class="px-4 py-3 text-sm table-cell-content">
                <div
                  contenteditable="true"
                  @blur="handleInlineEdit(term.id, 'sourceName', $event)"
                  @keydown.enter="handleInlineEditKeydown($event)"
                  class="editable-cell text-gray-900"
                >
                  {{ term.statue === 2 ? term.modifySourceName || term.sourceName : term.sourceName }}
                </div>
              </td>
              <td class="px-4 py-3 text-sm table-cell-content">
                <div
                  contenteditable="true"
                  @blur="handleInlineEdit(term.id, 'targetName', $event)"
                  @keydown.enter="handleInlineEditKeydown($event)"
                  class="editable-cell text-gray-900"
                >
                  <!-- The display value is now the modified name if it exists, otherwise the original target name -->
                  {{ term.statue === 2 ? term.modifyName : term.targetName }}
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>

    <!-- New Term Modal -->
    <div v-if="isModalOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900 bg-opacity-50">
      <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md transform transition-all sm:my-8 sm:align-middle sm:max-w-lg">
        <h3 class="text-xl font-semibold leading-6 text-gray-900 mb-4">添加新术语</h3>
        <div class="space-y-4">
          <div>
            <label for="newSourceName" class="block text-sm font-medium text-gray-700">原词</label>
            <input
              type="text"
              id="newSourceName"
              v-model="newSourceName"
              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
            >
          </div>
          <div>
            <label for="newTargetName" class="block text-sm font-medium text-gray-700">译词</label>
            <input
              type="text"
              id="newTargetName"
              v-model="newTargetName"
              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
            >
          </div>
        </div>
        <div class="mt-6 flex justify-end space-x-3">
          <button
            @click="cancelAddTerm"
            class="inline-flex justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            取消
          </button>
          <button
            @click="saveNewTerm"
            :disabled="!newSourceName || !newTargetName"
            class="inline-flex justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            保存
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import service from "@/api/axios";
import { ElMessage } from "element-plus";

export default {
  data() {
    return {
      clickButton: false,
      // `termsData` is now the single source of truth for all terms (original, deleted, modified)
      termsData: [],
      // `termsCache` stores fetched pages to prevent re-fetching
      termsCache: {},
      currentTab: 'original',
      selectedOriginalIds: [],
      selectedToDeleteIds: [],
      selectedToModifyIds: [],
      // New state for search results selections
      selectedSearchIds: [],
      currentPage: 1,
      itemsPerPage: 100,
      totalPages: 1, // Stores total pages for the original tab
      searchKeyword: '',
      searchResults: [],
      searchTotalPages: 1,
      
      // New data properties for the modal
      isModalOpen: false,
      newSourceName: '',
      newTargetName: ''
    };
  },
  computed: {
    // These computed properties now filter the single `termsData` array
    filteredOriginalTerms() {
      // The `original` tab now uses a paginated list, not a filtered one.
      // This computed property is still useful for other logic, but the table now uses `paginatedOriginalTerms`
      return this.termsData.filter(term => term.statue === 0);
    },
    filteredToDeleteTerms() {
      return this.termsData.filter(term => term.statue === 1);
    },
    filteredToModifyTerms() {
      return this.termsData.filter(term => term.statue === 2);
    },

    // Paginated terms for each tab
    paginatedOriginalTerms() {
      // Returns the terms for the current page from the `termsCache`
      // We use the cache to avoid re-rendering and keep a single source of truth
      return this.termsCache[this.currentPage] || [];
    },
    paginatedToDeleteTerms() {
      // The `toDelete` and `toModify` tabs are still client-side paginated
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.filteredToDeleteTerms.slice(start, end);
    },
    paginatedToModifyTerms() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.filteredToModifyTerms.slice(start, end);
    },

    // Total pages for the currently active tab
    currentTotalPages() {
      if (this.currentTab === 'original') {
        return this.totalPages;
      } else if (this.currentTab === 'toDelete') {
        return Math.max(1, Math.ceil(this.filteredToDeleteTerms.length / this.itemsPerPage));
      } else if (this.currentTab === 'toModify') {
        return Math.max(1, Math.ceil(this.filteredToModifyTerms.length / this.itemsPerPage));
      } else if (this.currentTab === 'search') {
        // The search page's total pages is determined by the API response
        return this.searchTotalPages;
      }
      return 1; // Fallback
    },

    // Computed properties for "Select All" checkboxes
    selectAllOriginal: {
      get() {
        const paginatedTerms = this.paginatedOriginalTerms;
        return paginatedTerms.length > 0 && paginatedTerms.every(term => this.selectedOriginalIds.includes(term.id));
      },
      set(value) {
        const paginatedTerms = this.paginatedOriginalTerms;
        if (value) {
          const currentIds = paginatedTerms.map(term => term.id);
          this.selectedOriginalIds = [...new Set([...this.selectedOriginalIds, ...currentIds])];
        } else {
          const currentIds = paginatedTerms.map(term => term.id);
          this.selectedOriginalIds = this.selectedOriginalIds.filter(id => !currentIds.includes(id));
        }
      }
    },
    selectAllToDelete: {
      get() {
        const paginatedTerms = this.paginatedToDeleteTerms;
        return paginatedTerms.length > 0 && paginatedTerms.every(term => this.selectedToDeleteIds.includes(term.id));
      },
      set(value) {
        const paginatedTerms = this.paginatedToDeleteTerms;
        if (value) {
          const currentIds = paginatedTerms.map(term => term.id);
          this.selectedToDeleteIds = [...new Set([...this.selectedToDeleteIds, ...currentIds])];
        } else {
          const currentIds = paginatedTerms.map(term => term.id);
          this.selectedToDeleteIds = this.selectedToDeleteIds.filter(id => !currentIds.includes(id));
        }
      }
    },
    selectAllToModify: {
      get() {
        const paginatedTerms = this.paginatedToModifyTerms;
        return paginatedTerms.length > 0 && paginatedTerms.every(term => this.selectedToModifyIds.includes(term.id));
      },
      set(value) {
        const paginatedTerms = this.paginatedToModifyTerms;
        if (value) {
          const currentIds = paginatedTerms.map(term => term.id);
          this.selectedToModifyIds = [...new Set([...this.selectedToModifyIds, ...currentIds])];
        } else {
          const currentIds = paginatedTerms.map(term => term.id);
          this.selectedToModifyIds = this.selectedToModifyIds.filter(id => !currentIds.includes(id));
        }
      }
    },
    // New computed property for the search tab's select all functionality
    selectAllSearch: {
      get() {
        const searchResults = this.searchResults;
        return searchResults.length > 0 && searchResults.every(term => this.selectedSearchIds.includes(term.id));
      },
      set(value) {
        const searchResults = this.searchResults;
        if (value) {
          const currentIds = searchResults.map(term => term.id);
          this.selectedSearchIds = [...new Set([...this.selectedSearchIds, ...currentIds])];
        } else {
          const currentIds = searchResults.map(term => term.id);
          this.selectedSearchIds = this.selectedSearchIds.filter(id => !currentIds.includes(id));
        }
      }
    }
  },
  methods: {
    /**
     * Handles fetching a specific page of original terms.
     * @param {number} page - The page number to fetch (1-based index).
     */
    async findOriginalTerms(page) {
      if (this.termsCache[page]) {
        console.log(`Page ${page} found in cache. Using cached data.`);
        return;
      }
      try {
        const response = await service.get(`/api/glossary/findAllByNovelIdAndStatue/${this.$route.params.id}/${page - 1}/${this.itemsPerPage}`);
        this.termsCache = {
          ...this.termsCache,
          [page]: response.data.content
        };
        this.totalPages = response.data.totalPages;
        const newTerms = response.data.content;
        newTerms.forEach(newTerm => {
          const existingTerm = this.termsData.find(t => t.id === newTerm.id);
          if (!existingTerm) {
            this.termsData.push(newTerm);
          }
        });
      } catch (error) {
        console.error("Error fetching original terms:", error);
      }
    },

    /**
     * Fetches a specific page of search results and updates the component state.
     * The `termsData` array is updated to ensure a single source of truth for all terms.
     * @param {number} page - The page number to fetch (1-based index).
     */
    async searchTerms(page) {
      if (!this.searchKeyword) {
        ElMessage.warning('请输入搜索关键词。');
        return;
      }
      try {
        const response = await service.get(`/api/glossary/searchAllByNovelIdAndStatue/${this.$route.params.id}/${page - 1}/${this.itemsPerPage}`, {
          params: { keyword: this.searchKeyword }
        });

        // Clear existing search results to display the new ones
        this.searchResults = response.data.content;
        this.searchTotalPages = response.data.totalPages;
        this.currentPage = page;

        // Merge the new search results into the main `termsData` array
        this.searchResults.forEach(searchResultTerm => {
          const existingTerm = this.termsData.find(t => t.id === searchResultTerm.id);
          if (!existingTerm) {
            // Add the new term to the main data array if it doesn't exist
            this.termsData.push(searchResultTerm);
          } else {
            // Update the existing term in the main data array with the search result data
            Object.assign(existingTerm, searchResultTerm);
          }
        });

        // Clear selections after a new search
        this.selectedSearchIds = [];

      } catch (error) {
        console.error("Error searching terms:", error);
        ElMessage.error("搜索失败，请重试。");
      }
    },

    /**
     * Handles tab switching.
     * @param {string} tabName - The name of the tab to activate ('original', 'toDelete', 'toModify', 'search').
     */
    switchTab(tabName) {
      this.currentTab = tabName;
      this.currentPage = 1;
      // Clear selections for all tabs when switching
      this.selectedOriginalIds = [];
      this.selectedToDeleteIds = [];
      this.selectedToModifyIds = [];
      this.selectedSearchIds = [];
      if (tabName === 'original') {
        this.findOriginalTerms(1);
      }
      this.$nextTick(() => {
        this.adjustCurrentPage();
      });
    },

    /**
     * Handles batch actions (delete, undoDelete, undoModify).
     * @param {string} actionType - The type of action ('delete', 'undoDelete', 'undoModify').
     */
    handleBatchAction(actionType) {
      let selectedIds = [];
      // Determine the source of selected IDs based on the current tab
      if (this.currentTab === 'original') {
        selectedIds = this.selectedOriginalIds;
      } else if (this.currentTab === 'toDelete') {
        selectedIds = this.selectedToDeleteIds;
      } else if (this.currentTab === 'toModify') {
        selectedIds = this.selectedToModifyIds;
      } else if (this.currentTab === 'search') {
        // Use the new search selected IDs
        selectedIds = this.selectedSearchIds;
      }

      if (selectedIds.length === 0) {
        ElMessage.warning('请选择至少一个术语进行操作。');
        return;
      }

      // Update the main `termsData` array based on the action
      this.termsData = this.termsData.map(term => {
        if (selectedIds.includes(term.id)) {
          if (actionType === 'delete') {
            if (term.statue === 0) {
              term.statue = 1; // Mark for deletion
            }
          } else if (actionType === 'undoDelete') {
            if (term.statue === 1) {
              term.statue = 0; // Restore to original
            }
          } else if (actionType === 'undoModify') {
            if (term.statue === 2) {
              term.statue = 0;
              term.modifyName = null; // Clear modified name
              term.modifySourceName = null; // Clear modified source name
            }
          }
        }
        return term;
      });

      // Also update the `searchResults` array if the action was on the search tab
      if (this.currentTab === 'search') {
        this.searchResults = this.searchResults.map(term => {
          const updatedTerm = this.termsData.find(t => t.id === term.id);
          return updatedTerm ? { ...term, ...updatedTerm } : term;
        });
      }

      // Clear selections for the current tab after batch action
      if (this.currentTab === 'original') {
        this.selectedOriginalIds = [];
      } else if (this.currentTab === 'toDelete') {
        this.selectedToDeleteIds = [];
      } else if (this.currentTab === 'toModify') {
        this.selectedToModifyIds = [];
      } else if (this.currentTab === 'search') {
        this.selectedSearchIds = [];
      }

      this.$nextTick(() => {
        this.adjustCurrentPage();
      });

      ElMessage.success('批量操作成功！');
    },

    /**
     * Adjusts the current page if it becomes invalid after data changes.
     */
    adjustCurrentPage() {
      const totalPages = this.currentTotalPages;
      if (this.currentPage > totalPages) {
        this.currentPage = Math.max(1, totalPages);
      }
    },

    /**
     * Handles inline editing when a contenteditable cell loses focus.
     * @param {number} termId - The ID of the term being edited.
     * @param {string} field - The name of the field being edited ('sourceName' or 'targetName').
     * @param {Event} event - The blur event from the contenteditable div.
     */
    handleInlineEdit(termId, field, event) {
      const newValue = event.target.textContent.trim();
      const term = this.termsData.find(t => t.id === termId);
      if (!term) return;

      let originalValue = term[field];
      if (field === 'sourceName' && term.modifySourceName) {
        originalValue = term.modifySourceName;
      }
      if (field === 'targetName' && term.modifyName) {
        originalValue = term.modifyName;
      }
      
      // Only update if the value has changed
      if (newValue === originalValue) {
        return;
      }

      // Update the term in the main `termsData` array
      const termIndex = this.termsData.findIndex(t => t.id === termId);
      if (termIndex !== -1) {
        // If the term is brand new and has a temp ID, we can directly change the sourceName
        // Otherwise, mark it as modified (statue 2)
        if (term.id.toString().startsWith('temp-')) {
          this.termsData[termIndex][field] = newValue;
        } else {
          if (field === 'sourceName') {
            this.termsData[termIndex].modifySourceName = newValue;
          } else if (field === 'targetName') {
            this.termsData[termIndex].modifyName = newValue;
          }
          this.termsData[termIndex].statue = 2;
        }
      }

      // If we are on the search tab, also update the searchResults view
      if (this.currentTab === 'search') {
        const searchTerm = this.searchResults.find(t => t.id === termId);
        if (searchTerm) {
          searchTerm.statue = 2;
          if (field === 'sourceName') {
            searchTerm.modifySourceName = newValue;
          } else if (field === 'targetName') {
            searchTerm.modifyName = newValue;
          }
        }
      }

      ElMessage.success(`术语 ID ${termId} 已标记为修改`);
    },

    /**
     * Handles keydown events for inline editable cells, specifically for Enter key.
     */
    handleInlineEditKeydown(event) {
      if (event.key === 'Enter') {
        event.preventDefault();
        event.target.blur();
      }
    },

    /**
     * Now, this method only opens the modal. The actual term creation is in `saveNewTerm`.
     */
    handleAddTerm() {
      this.newSourceName = '';
      this.newTargetName = '';
      this.isModalOpen = true;
    },
    
    /**
     * Saves the new term from the modal and adds it to the list.
     */
    saveNewTerm() {
      if (!this.newSourceName || !this.newTargetName) {
        ElMessage.warning('原词和译词都不能为空。');
        return;
      }
      // const tempId = `temp-${Date.now()}`;
      // const newTerm = {
      //   id: tempId,
      //   novelId: this.$route.params.id,
      //   sourceName: this.newSourceName,
      //   targetName: this.newTargetName,
      //   statue: 0,
      //   modifyName: null,
      //   modifySourceName: null
      // };
      service.post("/api/glossary/batch/" + this.$route.params.id,{
        title: this.newSourceName,
        content: this.newTargetName,
      }).then(res => {
        const newTerm = res.data;
        if (newTerm.statue == 100) {
          ElMessage.warning("该术语已存在，请勿重新提交");
        } else{
          this.termsData.unshift(newTerm);
                // Add the new term to the beginning of the termsData array
          this.termsData.unshift(newTerm);

          // Add to the cache for the current page
          if (!this.termsCache[this.currentPage]) {
            this.termsCache[this.currentPage] = [];
          }
          this.termsCache[this.currentPage].unshift(newTerm);

          // Close the modal and show success message
          this.isModalOpen = false;
          this.switchTab('original');
          ElMessage.success("新术语已添加，请编辑后点击同步。");
        }

      })
    },

    /**
     * Closes the modal without saving.
     */
    cancelAddTerm() {
      this.isModalOpen = false;
    },
    
    /**
     * Handles the click event for the back button.
     */
    urlPush() {
      window.history.back();
    },

    /**
     * Handles the click event for the sync button.
     * This method correctly processes all changes because `termsData` is the single source of truth.
     */
    async handleSync() {
      this.clickButton = true;
      try {
        const modifiedTerms = this.filteredToModifyTerms.map(({ id, novelId, modifyName, modifySourceName }) => ({
          id,
          novelId,
          modifyName,
          modifySourceName
        }));
        const deletedTermIds = this.filteredToDeleteTerms.map(item => item.id);

        await service.post("/api/glossary/processTerminology", {
          novelId: this.$route.params.id,
          paginatedToDeleteTerms: deletedTermIds,
          paginatedToModifyTerms: modifiedTerms
        });

        // Reset state after a successful sync
        this.termsData = this.termsData.filter(term => term.statue === 0);
        this.termsCache = {};
        this.searchResults = [];

        this.switchTab('original');
        ElMessage.success("同步成功");
      } catch (error) {
        console.error("同步失败:", error);
        ElMessage.error("同步失败，请重试");
      } finally {
        this.clickButton = false;
      }
    },

    /**
     * Navigates to the next page.
     */
    nextPage() {
      if (this.currentPage < this.currentTotalPages) {
        this.currentPage++;
        if (this.currentTab === 'original') {
          // You could add this, but the cache should prevent redundant fetches
          // this.findOriginalTerms(this.currentPage);
        } else if (this.currentTab === 'search') {
          this.searchTerms(this.currentPage);
        }
      }
    },

    /**
     * Navigates to the previous page.
     */
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
        if (this.currentTab === 'original') {
          // You could add this, but the cache should prevent redundant fetches
          // this.findOriginalTerms(this.currentPage);
        } else if (this.currentTab === 'search') {
          this.searchTerms(this.currentPage);
        }
      }
    }
  },
  mounted() {
    this.findOriginalTerms(1); // Initial fetch for the first page
  }
};
</script>

<style>
/* Global styles for the body and scrollbar */
body {
  font-family: 'Inter', sans-serif;
  background-color: #f3f4f6; /* Light gray background */
}
.table-container::-webkit-scrollbar {
  height: 8px;
}
.table-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}
.table-container::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 10px;
}
.table-container::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* Style for editable cells */
.editable-cell {
  min-width: 50px; /* Ensure editable area has a minimum width */
  padding: 0.5rem 0.75rem; /* Padding for better click area */
  border-radius: 0.375rem; /* rounded-md */
  transition: background-color 0.15s ease-in-out;
  white-space: normal; /* Ensure text wraps within editable cell */
  word-break: break-word; /* Break long words */
}
.editable-cell:hover {
  background-color: #f9fafb; /* hover:bg-gray-50 */
}
.editable-cell:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.5); /* focus:ring-2 focus:ring-blue-500 */
}

/* Default table cell styling to allow wrapping and min-width */
.table-cell-content {
  white-space: normal;
  word-break: break-word;
  min-width: 50px; /* Ensure all table cells have a minimum width for 3 characters */
}
</style>
