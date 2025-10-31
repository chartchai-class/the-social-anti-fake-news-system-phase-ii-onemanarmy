package se331.backend.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se331.backend.entity.CreateCommentRequest;
import se331.backend.entity.CreateNewsRequest;
import se331.backend.entity.NewsDTO;
import se331.backend.service.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;

import java.util.List;

/**
 * รับ HTTP requests จาก Frontend และส่งต่อไปยัง Service Layer
 */
@RestController // บอก Spring ว่านี่คือ REST API Controller (ส่ง JSON กลับไป)
@RequestMapping("/api/news") // URL base path คือ /api/news
@CrossOrigin(origins = "*") // อนุญาตให้ Frontend จาก domain อื่นเรียกใช้ได้ (เช่น localhost:5173)
public class NewsController {

    @Autowired // ให้ Spring inject NewsService เข้ามาอัตโนมัติ (Dependency Injection)
    private NewsService newsService;

    /**
     * API: GET /api/news
     * ดึงข่าวทั้งหมด (ไม่รวมข่าวที่ถูกลบ สำหรับ normal user)
     *
     * @return ResponseEntity<List<NewsDTO>> - รายการข่าวทั้งหมดพร้อม HTTP status 200 OK
     */
    @GetMapping // รับ HTTP GET request ที่ /api/news
    public ResponseEntity<List<NewsDTO>> getAllNews() {
        // เรียก service เพื่อดึงข่าวทั้งหมด
        List<NewsDTO> newsList = newsService.getAllNews();

        // ส่งข้อมูลกลับพร้อม HTTP status 200 OK
        return ResponseEntity.ok(newsList);
    }

    /**
     * API: GET /api/news/removed
     * ดึงข่าวที่ถูกลบแล้ว (เฉพาะ Admin เท่านั้น)
     *
     * @return ResponseEntity<List<NewsDTO>> - รายการข่าวที่ถูกลบ
     */
    @GetMapping("/removed") // รับ GET request ที่ /api/news/removed
    public ResponseEntity<List<NewsDTO>> getRemovedNews() {
        List<NewsDTO> removedNews = newsService.getRemovedNews();
        return ResponseEntity.ok(removedNews);
    }

    /**
     * API: GET /api/news/{id}
     * ดึงข้อมูลข่าวตาม ID
     *
     * @param id - ID ของข่าวที่ต้องการดึง (มาจาก URL path)
     * @return ResponseEntity<NewsDTO> - ข้อมูลข่าวที่ต้องการ
     */
    @GetMapping("/{id}") // {id} คือ path variable (ค่าที่ส่งมาใน URL)
    public ResponseEntity<NewsDTO> getNewsById(@PathVariable Long id) {
        // @PathVariable บอกว่าเอาค่า id จาก URL path
        // เช่น GET /api/news/5 จะได้ id = 5
        NewsDTO news = newsService.getNewsById(id);
        return ResponseEntity.ok(news);
    }

    /**
     * API: POST /api/news
     * สร้างข่าวใหม่
     *
     * @param request - ข้อมูลข่าวที่จะสร้าง (รับมาเป็น JSON จาก request body)
     * @return ResponseEntity<NewsDTO> - ข่าวที่สร้างเสร็จพร้อม HTTP status 201 CREATED
     */
    @PostMapping // รับ HTTP POST request ที่ /api/news
    public ResponseEntity<NewsDTO> createNews(@RequestBody CreateNewsRequest request) {
        // @RequestBody แปลง JSON ที่ส่งมาเป็น Java object (CreateNewsRequest)
        NewsDTO createdNews = newsService.createNews(request);

        // ส่งกลับพร้อม HTTP status 201 CREATED (บอกว่าสร้างสำเร็จ)
        return new ResponseEntity<>(createdNews, HttpStatus.CREATED);
    }

    /**
     * API: POST /api/news/{id}/comments
     * เพิ่ม comment (และ vote) ให้กับข่าว
     *
     * @param request - ข้อมูล comment ที่จะเพิ่ม (JSON จาก request body)
     * @return ResponseEntity<NewsDTO> - ข้อมูลข่าวที่อัปเดตแล้ว (มี comment ใหม่)
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<NewsDTO> addComment(
            @PathVariable("id") Long newsId, // ดึง id จาก URL path
            @RequestBody CreateCommentRequest request) { // ดึงข้อมูล comment จาก request body

        NewsDTO updatedNews = newsService.addCommentToNews(newsId, request);
        return ResponseEntity.ok(updatedNews);
    }

    /**
     * API: DELETE /api/news/{id}
     * ลบข่าว (Soft Delete - แค่ทำเครื่องหมายว่าถูกลบ ไม่ลบจริงๆ ออกจาก database)
     *
     * @param id - ID ของข่าวที่จะลบ
     * @return ResponseEntity<Void> - HTTP status 204 NO CONTENT (ลบสำเร็จ ไม่มีข้อมูลส่งกลับ)
     */
    @DeleteMapping("/{id}") // รับ HTTP DELETE request
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);

        // noContent() = HTTP 204 (สำเร็จแต่ไม่มีข้อมูลตอบกลับ)
        return ResponseEntity.noContent().build();
    }

    /**
     * API: DELETE /api/news/{newsId}/comments/{commentId}
     * ลบ comment ออกจากข่าว
     *
     * @return ResponseEntity<Void> - HTTP status 204 NO CONTENT
     */
    @DeleteMapping("/{newsId}/comments/{commentId}")
    public ResponseEntity<Void> deleteCommentFromNews(
            @PathVariable Long newsId,    // ดึง newsId จาก URL
            @PathVariable Long commentId) { // ดึง commentId จาก URL

        newsService.deleteCommentFromNews(newsId, commentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * API: GET /api/news/search
     * ค้นหาและกรองข่าวแบบมี Pagination
     *
     * @param title - keyword สำหรับค้นหา (ค้นหาใน topic, shortDetail, reporter)
     * @param status - สถานะที่ต้องการกรอง (real, fake, equal, removed)
     * @param perPage - จำนวนข่าวต่อหน้า (default = 10)
     * @param page - หน้าที่ต้องการดึง เริ่มจาก 1 (default = 1)
     * @return ResponseEntity<?> - รายการข่าวพร้อม header x-total-count
     */
    @GetMapping("/search") // รับ GET request ที่ /api/news/search
    public ResponseEntity<?> getNews(
            @RequestParam(value = "title", required = false) String title,
            // @RequestParam ดึงค่าจาก query parameter (?title=...)
            // required = false หมายความว่าไม่บังคับต้องส่งมา (ถ้าไม่ส่งจะเป็น null)

            @RequestParam(value = "status", required = false) String status,
            // กรองตามสถานะข่าว (real/fake/equal/removed)

            @RequestParam(value = "_limit", required = false) Integer perPage,
            // จำนวนข่าวต่อหน้า (_limit ตาม JSON API convention)

            @RequestParam(value = "_page", required = false) Integer page,
        // หมายเลขหน้าที่ต้องการ (_page ตาม JSON API convention)

            @RequestParam(value = "_sort", required = false) String sortField,
            @RequestParam(value = "_order", required = false) String sortOrder) {

        // กำหนดค่า default ถ้าไม่มีการส่งมา
        perPage = perPage == null ? 10 : perPage; // ถ้าไม่ส่ง _limit มา ให้ใช้ 10
        page = page == null ? 1 : page; // ถ้าไม่ส่ง _page มา ให้ใช้ 1 (หน้าแรก)

        sortField = sortField == null ? "dateTime" : sortField;
        sortOrder = sortOrder == null ? "desc" : sortOrder;

        Sort.Direction direction = sortOrder.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);

        Pageable pageable = PageRequest.of(page - 1, perPage, sort);

        // เรียก service เพื่อค้นหาและกรองข่าว
        // PageRequest.of(page - 1, perPage) = สร้าง pagination object
        // - page - 1 เพราะ Spring เริ่มนับหน้าจาก 0 (แต่ Frontend ส่งมาเริ่มที่ 1)
        // - perPage = จำนวนข่าวต่อหน้า
        Page<NewsDTO> pageOutput = newsService.getNews(title, status, pageable);

        // สร้าง HTTP Headers เพื่อส่งข้อมูลเพิ่มเติม
        HttpHeaders responseHeader = new HttpHeaders();

        // เพิ่ม header x-total-count เพื่อบอก Frontend ว่ามีข่าวทั้งหมดกี่รายการ
        // (ใช้สำหรับคำนวณจำนวนหน้าทั้งหมด)
        responseHeader.set("x-total-count", String.valueOf(pageOutput.getTotalElements()));
        // getTotalElements() = จำนวนข่าวทั้งหมดที่ตรงกับเงื่อนไข

        // ส่งข้อมูลกลับ:
        // - pageOutput.getContent() = รายการข่าวในหน้านี้ (List<NewsDTO>)
        // - responseHeader = HTTP headers (มี x-total-count)
        // - HttpStatus.OK = HTTP 200 (สำเร็จ)
        return new ResponseEntity<>(pageOutput.getContent(), responseHeader, HttpStatus.OK);
    }
}