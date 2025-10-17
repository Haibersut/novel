import { createRouter, createWebHistory } from 'vue-router';
import {containsChinese, getTitleText, getTitleText1, getTitleText2} from "@/api/axios";
import {ElMessage} from "element-plus";
const WebLibrary = () => import(/* webpackChunkName: "FeedBack" */ '../components/WebLibrary.vue')
const WebSearch = () => import(/* webpackChunkName: "FeedBack" */ '../components/WebSearch.vue')
const WebFavorites = () => import(/* webpackChunkName: "FeedBack" */ '../components/WebFavorites.vue')
const ChapterDetail = () => import(/* webpackChunkName: "FeedBack" */ '../components/ChapterDetail.vue')
const NovelDetail = () => import(/* webpackChunkName: "FeedBack" */ '../components/NovelDetail.vue')
const UserDetail = () => import(/* webpackChunkName: "FeedBack" */ '../components/UserDetail.vue')
const WebLogin = () => import(/* webpackChunkName: "WebLogin" */ '../components/WebLogin.vue')
const RecommendationList = () => import(/* webpackChunkName: "FeedBack" */ '../components/RecommendationList.vue')
const RecommendationDetail = () => import(/* webpackChunkName: "FeedBack" */ '../components/RecommendationDetail.vue')
const WebNote = () => import(/* webpackChunkName: "FeedBack" */ '../components/WebNote.vue')
const NoteDetail = () => import(/* webpackChunkName: "FeedBack" */ '../components/NoteDetail.vue')
const SyosetuNovel = () => import(/* webpackChunkName: "FeedBack" */ '../components/SyosetuNovel.vue')
const WebNovelPlatform = () => import(/* webpackChunkName: "FeedBack" */ '../components/WebNovelPlatform.vue')
const NovelPiaNovel = () => import(/* webpackChunkName: "FeedBack" */ '../components/NovelPiaNovel.vue')
const UploadAndShare = () => import(/* webpackChunkName: "FeedBack" */ '../components/UploadAndShare.vue')
const UploadNovelDetail = () => import(/* webpackChunkName: "FeedBack" */ '../components/UploadNovelDetail.vue')
const UploadChapterAdmin = () => import(/* webpackChunkName: "FeedBack" */ '../components/UploadChapterAdmin.vue')
const UploadChapterEdit = () => import(/* webpackChunkName: "FeedBack" */ '../components/UploadChapterEdit.vue')
const UploadNovelEdit = () => import(/* webpackChunkName: "FeedBack" */ '../components/UploadNovelEdit.vue')
const WebHistory = () => import(/* webpackChunkName: "FeedBack" */ '../components/WebHistory.vue')
const TagFilter = () => import(/* webpackChunkName: "FeedBack" */ '../components/TagFilter.vue')
const GlossaryPage = () => import(/* webpackChunkName: "FeedBack" */ '../components/GlossaryPage.vue')
const UserGlossaryPage = () => import(/* webpackChunkName: "FeedBack" */ '../components/UserGlossaryPage.vue')
const ModifyPassword = () => import(/* webpackChunkName: "FeedBack" */ '../components/ModifyPassword.vue')
const BlacklistPage = () => import(/* webpackChunkName: "FeedBack" */ '../components/BlacklistPage.vue')
const WebStore = () => import(/* webpackChunkName: "FeedBack" */ '../components/WebStore.vue')
const MessageView = () => import(/* webpackChunkName: "FeedBack" */ '../components/MessageView.vue')
const InfoList = () => import(/* webpackChunkName: "FeedBack" */ '../components/InfoList.vue')
const WriterDetail = () => import(/* webpackChunkName: "FeedBack" */ '../components/WriterDetail.vue')
const WebLibraryNp = () => import(/* webpackChunkName: "FeedBack" */ '../components/WebLibraryNp.vue')
const routes = [
    {
        path: '/infoList',
        name: 'InfoList',
        component: InfoList,
    },
    {
        path: '/webLibraryNp',
        name: 'WebLibraryNp',
        component: WebLibraryNp,
    },
    {
        path: '/writerDetail/:id',
        name: 'WriterDetail',
        component: WriterDetail,
        meta: { hideApp: true }
    },
    {
        path: '/userGlossaryPage/:id',
        name: 'UserGlossaryPage',
        component: UserGlossaryPage,
        meta: { hideApp: true }
    },
    {
        path: '/webLibrary',
        name: 'WebLibrary',
        component: WebLibrary,
    },
    {
        path: '/blacklistPage',
        name: 'BlacklistPage',
        component: BlacklistPage,
    },
    {
        path: '/messageView',
        name: 'MessageView',
        component: MessageView,
    },
    {
        path: '/webStore',
        name: 'WebStore',
        component: WebStore,
    },
    {
        path: '/glossaryPage/:id',
        name: 'GlossaryPage',
        component: GlossaryPage,
        meta: { hideApp: true }
    },
    {
        path: '/modifyPassword',
        name: 'ModifyPassword',
        component: ModifyPassword,
    },
    {
        path: '/search',
        name: 'WebSearch',
        component: WebSearch,
        meta: { hideSearch: true }
    },
    {
        path: '/uploadNovelDetail',
        name: 'UploadNovelDetail',
        component: UploadNovelDetail,
        meta: { hideSearch: true }
    },
    {
        path: '/uploadNovelEdit/:id',
        name: 'UploadNovelEdit',
        component: UploadNovelEdit,
        meta: { hideSearch: true }
    },
    {
        path: '/uploadChapterAdmin/:id',
        name: 'UploadChapterAdmin',
        component: UploadChapterAdmin,
        meta: { hideSearch: true }
    },
    {
        path: '/tagFilter',
        name: 'TagFilter',
        component: TagFilter,
    },
    {
        path: '/uploadChapterEdit/:id',
        name: 'UploadChapterEdit',
        component: UploadChapterEdit,
        meta: { hideApp: true }
    },
    {
        path: '/uploadAndShare',
        name: 'UploadAndShare',
        component: UploadAndShare,
        meta: { hideSearch: true }
    },
    {
        path: '/syosetuNovel',
        name: 'SyosetuNovel',
        component: SyosetuNovel,
        meta: { hideSearch: true }
    },
    {
        path: '/novelPiaNovel',
        name: 'NovelPiaNovel',
        component: NovelPiaNovel,
        meta: { hideSearch: true }
    },
    {
        path: '/novelPlatform',
        name: 'WebNovelPlatform',
        component: WebNovelPlatform,
    },
    {
        path: '/noteDetail/:id',
        name: 'NoteDetail',
        component: NoteDetail,
        meta: { hideSearch: true }
    },
    {
        path: '/webNote',
        name: 'WebNote',
        component: WebNote,
        meta: { hideSearch: true }
    },
    {
        path: '/',
        name: 'RecommendationList',
        component: RecommendationList,
    },
    {
        path: '/recommendationDetail/:id',
        name: 'RecommendationDetail',
        component: RecommendationDetail,
    },
    {
        path: '/login',
        name: 'WebLogin',
        component: WebLogin,
    },
    {
        path: '/webHistory',
        name: 'WebHistory',
        component: WebHistory,
    },
    {
        path: '/favorites',
        name: 'WebFavorites',
        component: WebFavorites,
    },
    {
        path: '/chapterDetail/:id',
        name: 'ChapterDetail',
        component: ChapterDetail,
        meta: { hideApp: true}
    },
    {
        path: '/novelDetail/:id',
        name: 'NovelDetail',
        component: NovelDetail,
    },
    {
        path: '/userDetail',
        name: 'UserDetail',
        component: UserDetail,
    },
];

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes,
});
const okUrl = ['WebLogin','WebSearch','RecommendationList','RecommendationDetail','WebLibrary','NovelDetail','ChapterDetail']
const isExists = (value) => okUrl.includes(value);
// 添加路由守卫
router.beforeEach((to, from, next) => {
    let newVar = getTitleText1() + getTitleText() + getTitleText2();
    if (newVar.length > 0 && !containsChinese(newVar)) {
        ElMessage.error("system error")
        return Promise.reject(new Error('system error'));
    }
    const token = localStorage.getItem('Authorization');
    if ((!token || token === 'undefined') && (!isExists(to.name))) {
        next({ name: 'WebLogin' }); // 如果未登录或 token 无效，跳转到登录页面
    } else {
        next();
    }
});
router.afterEach(() => {

    document.documentElement.scrollTop = 0;
})

export default router;