<template>
  <div class="note-detail-container">
    <div class="book-header-abc">
      <h1 class="book-title-abc" @click="goToNovel">{{ bookTitle }}</h1>
      <div class="book-meta-abc">
        <div class="meta-item-abc">
          <span class="meta-label-abc">共{{ totalNotes }}条笔记</span>
        </div>
      </div>
    </div>

    <div class="chapter-list-abc">
      <div
          v-for="(chapter, index) in chapters"
          :key="index"
          class="chapter"
      >
        <div class="chapter-header-abc">
          <h2 class="chapter-title-abc">第{{ chapter.chapterNumber }}章</h2>
        </div>
        <div
            v-for="(paragraph, pIndex) in chapter.notes"
            :key="pIndex"
            class="note-box-abc"
        >
          <div class="note-selector-abc">
            <input
                type="checkbox"
                :id="`note-${index}-${pIndex}`"
                v-model="chapter.selected[pIndex]"
            >
            <label :for="`note-${index}-${pIndex}`"></label>
          </div>
          <p class="note-content-abc">{{ paragraph.content }}</p>
        </div>
      </div>
    </div>

    <!-- 悬浮菜单 -->
    <div class="floating-menu-abc" :class="{ 'show': showOptions }">
      <button class="menu-button-abc" @click="toggleOptions">
        <span class="menu-icon-abc">⋮</span>
      </button>
      <div class="menu-dropdown-abc">
        <button class="dropdown-button-abc" @click="copySelectedNotes">复制选中笔记</button>
        <button class="dropdown-button-abc" @click="selectAllNotes">全选</button>
        <button class="dropdown-button-abc" @click="clearSelection">取消选择</button>
        <button class="dropdown-button-abc" @click="deleteSelectedNotes">删除</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus';
import service from '@/api/axios';

export default {
  data() {
    return {
      bookTitle: null,
      readProgress: 22,
      totalNotes: 103,
      readTime: '11小时23分',
      showOptions: false,
      chapters: null,
    };
  },
  methods: {
    groupByChapter() {
      let id = this.$route.params.id;
      service.get(`/api/notes/groupByChapter/${id}`)
          .then(response => {
            this.bookTitle = response.data.novelTitle;
            this.totalNotes = response.data.totalNotes;
            this.chapters = response.data.noteGroups.map(group => ({
              ...group,
              selected: new Array(group.notes.length).fill(false),
            }));
          })
          .catch(error => console.error('Error fetching chapter:', error));
    },
    toggleOptions() {
      this.showOptions = !this.showOptions;
    },
    copySelectedNotes() {
      let notesText = '';
      let selectedCount = 0;

      this.chapters.forEach(chapter => {
        chapter.notes.forEach((note, pIndex) => {
          if (chapter.selected[pIndex]) {
            notesText += `【第${chapter.chapterNumber}章】\n${note.content}\n\n`;
            selectedCount++;
          }
        });
      });

      if (selectedCount > 0) {
        const textArea = document.createElement('textarea');
        textArea.value = notesText.trim();
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
        ElMessage.success(`已复制 ${selectedCount} 条笔记到剪贴板`);
      } else {
        ElMessage.warning('请至少选择一条笔记');
      }
    },
    selectAllNotes() {
      this.chapters.forEach(chapter => {
        chapter.selected.fill(true);
      });
    },
    clearSelection() {
      this.chapters.forEach(chapter => {
        chapter.selected.fill(false);
      });
    },
    async deleteSelectedNotes() {
      const selectedIds = this.chapters.flatMap(chapter =>
          chapter.notes
              .filter((_, index) => chapter.selected[index])
              .map(note => note.id)
      );

      if (selectedIds.length === 0) {
        ElMessage.warning('请先选择要删除的笔记');
        return;
      }

      try {
        await ElMessageBox.confirm(
            `确定要删除选中的 ${selectedIds.length} 条笔记吗？`,
            '删除确认',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning',
            }
        );

        const response = await service.post('/api/notes/delete', {
          ids: selectedIds,
        });

        if (response.data.success) {
          ElMessage.success('删除成功');

          this.chapters = this.chapters.map(chapter => {
            const remainingNotes = chapter.notes.filter(
                (_, index) => !chapter.selected[index]
            );

            return {
              ...chapter,
              notes: remainingNotes,
              selected: new Array(remainingNotes.length).fill(false),
            };
          });

          this.totalNotes -= selectedIds.length;
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error);
          ElMessage.error('删除操作失败');
        }
      }
    },
    goToNovel() {
      this.$router.push({ name: 'NovelDetail', params: { id: this.$route.params.id } });
    },
  },
  mounted() {
    this.groupByChapter();
  },
};
</script>

<style scoped>
.note-detail-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  background-color: #fff;
  min-height: 100vh;
}

.book-header-abc {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.book-title-abc {
  font-size: 22px;
  margin-bottom: 10px;
  color: #333;
  font-weight: 500;
}

.book-meta-abc {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.meta-item-abc {
  display: flex;
  align-items: center;
}

.chapter-header-abc {
  margin: 30px 0 15px;
  padding-top: 20px;
  border-top: 1px solid #f5f5f5;
}

.chapter-title-abc {
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.note-box-abc {
  margin-bottom: 15px;
  padding: 12px 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  display: flex;
  align-items: flex-start;
}

.note-selector-abc {
  margin-right: 10px;
  margin-top: 3px;
}

.note-selector-abc input[type="checkbox"] {
  display: none;
}

.note-selector-abc label {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 1px solid #ccc;
  border-radius: 3px;
  cursor: pointer;
  position: relative;
}

.note-selector-abc input[type="checkbox"]:checked + label::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #4285f4;
  font-size: 12px;
}

.note-content-abc {
  line-height: 1.6;
  color: #333;
  font-size: 15px;
  margin: 0;
  flex: 1;
}

/* 悬浮菜单样式 */
.floating-menu-abc {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 100;
}

.menu-button-abc {
  width: 40px;
  height: 40px;
  background-color: #4285f4;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  border: none;
  cursor: pointer;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  z-index: 101; /* 确保悬浮按钮在菜单之上 */
}

.menu-icon-abc {
  font-size: 24px;
}

.menu-dropdown-abc {
  position: absolute;
  bottom: 50px; /* 调整位置，确保不会被悬浮按钮遮挡 */
  right: 0;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: none;
  flex-direction: column;
  gap: 5px;
  width: 150px;
  z-index: 100; /* 确保下拉菜单在悬浮按钮之上 */
}

.floating-menu-abc.show .menu-dropdown-abc {
  display: flex;
}

.dropdown-button-abc {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  background-color: #f5f5f5;
  color: #333;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.dropdown-button-abc:hover {
  background-color: #e9e9e9;
}

/* 确保在移动端不会超出屏幕 */
@media (max-width: 480px) {
  .menu-dropdown-abc {
    right: 0;
    width: 100%;
    max-width: 300px;
  }
}

@media (max-width: 768px) {
  .book-title-abc {
    font-size: 20px;
  }

  .book-meta-abc {
    gap: 10px;
  }

  .chapter-title-abc {
    font-size: 16px;
  }

  .note-content-abc {
    font-size: 14px;
  }
}
</style>